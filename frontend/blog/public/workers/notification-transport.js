/**
 * SharedWorker that owns the one STOMP-over-WebSocket connection to Notification Service for the whole
 * browser, so every tab of this origin shares a single socket instead of one per tab.
 *
 * This file lives in `public/`: it is copied verbatim, never compiled, never type-checked, and cannot import
 * application code or npm packages. That is why the STOMP framing below is hand-written and why the socket
 * URL and the JWT arrive through messages: a worker scope has no `localStorage` to read the token from.
 */

/**
 * Escapes a STOMP 1.2 header key or value. Backslash must be doubled first, otherwise the substitutions
 * below would re-escape their own output.
 */
function escapeHeaderValue(value) {
  return value
    .replace(/\\/g, '\\\\')
    .replace(/\r/g, '\\r')
    .replace(/\n/g, '\\n')
    .replace(/:/g, '\\c');
}

function unescapeHeaderValue(value) {
  return value.replace(/\\(.)/g, (match, character) => {
    if (character === 'r') return '\r';
    if (character === 'n') return '\n';
    if (character === 'c') return ':';
    return character;
  });
}

function encodeFrame(command, headers, body) {
  let frame = command + '\n';

  for (const key of Object.keys(headers)) {
    const value = headers[key];
    if (value === undefined || value === null) continue;

    frame += escapeHeaderValue(key) + ':' + escapeHeaderValue(String(value)) + '\n';
  }

  frame += '\n';
  if (body) frame += body;

  return frame + '\u0000';
}

/**
 * Accumulates WebSocket chunks and yields complete frames. Heartbeat ticks arrive as bare `\n` / `\r\n`
 * (and some brokers as a lone NUL), so leading separators are discarded before a command is read.
 *
 * `content-length` is deliberately not honoured: frame bodies are STOMP messages serialized by Spring from
 * JSON, which never contains a raw NUL, so NUL termination is unambiguous and byte-offset tracking would
 * only add a place to be wrong about UTF-8.
 */
function createFrameBuffer() {
  let pending = '';

  return {
    reset() {
      pending = '';
    },

    push(chunk) {
      pending += chunk;

      const frames = [];
      let boundary = pending.indexOf('\u0000');

      while (boundary !== -1) {
        const raw = pending.slice(0, boundary);
        pending = pending.slice(boundary + 1);

        const frame = decodeFrame(raw.replace(/^[\r\n\u0000]+/, ''));
        if (frame) frames.push(frame);

        boundary = pending.indexOf('\u0000');
      }

      return frames;
    },
  };
}

function decodeFrame(raw) {
  if (!raw) return null;

  const headerEnd = raw.indexOf('\n\n');
  const head = headerEnd === -1 ? raw : raw.slice(0, headerEnd);
  const body = headerEnd === -1 ? '' : raw.slice(headerEnd + 2);

  const lines = head.split(/\r?\n/);
  const command = lines.shift();
  if (!command) return null;

  const headers = {};
  for (const line of lines) {
    if (!line) continue;

    const separator = line.indexOf(':');
    if (separator === -1) continue;

    headers[unescapeHeaderValue(line.slice(0, separator))] = unescapeHeaderValue(
      line.slice(separator + 1),
    );
  }

  return { command, headers, body };
}

/* ------------------------------------------------------------------------------------------------ *
 * Worker state
 * ------------------------------------------------------------------------------------------------ */

const DESTINATION = '/user/queue/notifications';
const MIN_RETRY_DELAY = 1000;
const MAX_RETRY_DELAY = 30000;
/** A rejected handshake looks identical to a dead server from here, so give up only after a few tries. */
const MAX_CONNECT_FAILURES = 2;

const ports = new Set();
const frames = createFrameBuffer();

let socket = null;
let token = null;
let socketUrl = null;
let state = 'closed';
let connected = false;
let connectFailures = 0;
let retryDelay = MIN_RETRY_DELAY;
let retryTimer = null;
let closingDeliberately = false;

function clientCount() {
  return ports.size;
}

function postToAll(message) {
  for (const port of ports) port.postMessage(message);
}

function setState(next, extra) {
  state = next;
  postToAll({ type: 'status', status: next, clients: clientCount(), ...extra });
}

function clearRetry() {
  if (retryTimer !== null) {
    clearTimeout(retryTimer);
    retryTimer = null;
  }
}

function scheduleRetry() {
  if (retryTimer !== null) return;

  const delay = Math.round(retryDelay * (0.75 + Math.random() * 0.5));
  retryDelay = Math.min(retryDelay * 2, MAX_RETRY_DELAY);

  setState('reconnecting', { nextRetryAt: Date.now() + delay });
  retryTimer = setTimeout(() => {
    retryTimer = null;
    openSocket();
  }, delay);
}

function sendFrame(command, headers, body) {
  if (!socket || socket.readyState !== WebSocket.OPEN) return false;

  socket.send(encodeFrame(command, headers, body));
  return true;
}

function closeSocket() {
  clearRetry();

  if (!socket) return;

  closingDeliberately = true;
  if (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING) {
    if (socket.readyState === WebSocket.OPEN)
      sendFrame('DISCONNECT', { receipt: 'quit-' + Date.now() });
    socket.close();
  }
  socket = null;
  connected = false;
  frames.reset();
}

function openSocket() {
  if (!socketUrl || !token) return;

  clearRetry();
  if (socket) {
    // Synchronous on the worker thread, so the new socket below cannot race the closing one.
    closeSocket();
  }

  const attempt = socketUrl + '?access_token=' + encodeURIComponent(token);

  setState('connecting');
  const ws = new WebSocket(attempt);
  socket = ws;

  ws.onopen = () => {
    // heart-beat 0,0 negotiates no heartbeats in either direction, which keeps Spring's simple broker from
    // scheduling heart-miss checks and spares this worker a keep-alive timer.
    sendFrame('CONNECT', {
      'accept-version': '1.2',
      host: new URL(attempt).host,
      'heart-beat': '0,0',
    });
  };

  ws.onmessage = (event) => {
    for (const frame of frames.push(String(event.data))) handleFrame(frame);
  };

  ws.onerror = () => {
    postToAll({ type: 'error', code: 'network', message: 'WebSocket transport failed.' });
  };

  ws.onclose = () => {
    if (socket === ws) {
      socket = null;
      frames.reset();
    }

    const wasConnected = connected;
    connected = false;
    if (closingDeliberately) {
      closingDeliberately = false;
      return;
    }

    if (wasConnected) {
      connectFailures = 0;
      retryDelay = MIN_RETRY_DELAY;
      scheduleRetry();
      return;
    }

    connectFailures += 1;
    if (connectFailures >= MAX_CONNECT_FAILURES) {
      setState('closed');
      postToAll({ type: 'auth-suspected' });
      return;
    }

    scheduleRetry();
  };
}

function handleFrame(frame) {
  switch (frame.command) {
    case 'CONNECTED': {
      connected = true;
      connectFailures = 0;
      retryDelay = MIN_RETRY_DELAY;
      sendFrame('SUBSCRIBE', { id: 'sub-0', destination: DESTINATION, ack: 'auto' });
      setState('connected');
      break;
    }
    case 'MESSAGE': {
      let notification = null;
      try {
        notification = JSON.parse(frame.body);
      } catch {
        postToAll({
          type: 'error',
          code: 'parse',
          message: 'Notification frame body is not JSON.',
        });
        break;
      }

      if (notification) postToAll({ type: 'notification', notification });
      break;
    }
    case 'ERROR': {
      postToAll({
        type: 'error',
        code: 'network',
        message: frame.headers.message || 'STOMP error.',
      });
      closeSocket();
      setState('closed');
      break;
    }
    default:
      break;
  }
}

/**
 * The token is the only thing that can revive a connection this worker has given up on, so a new value
 * always resets the failure budget and reconnects immediately.
 */
function setToken(next, nextSocketUrl) {
  if (nextSocketUrl) socketUrl = nextSocketUrl;

  if (!next) {
    token = null;
    closeSocket();
    setState('closed');
    return;
  }

  if (next === token && socket) return;

  token = next;
  connectFailures = 0;
  retryDelay = MIN_RETRY_DELAY;
  openSocket();
}

self.onconnect = (event) => {
  const port = event.ports[0];
  ports.add(port);

  port.onmessage = (message) => {
    const data = message.data || {};

    switch (data.type) {
      case 'token':
        setToken(data.token, data.socketUrl);
        break;
      case 'connect':
        port.postMessage({ type: 'hello', status: state, clients: clientCount() });
        break;
      case 'ping':
        port.postMessage({ type: 'status', status: state, clients: clientCount() });
        break;
      default:
        break;
    }
  };

  port.onclose = () => {
    ports.delete(port);
    setState(state);
  };

  port.postMessage({ type: 'hello', status: state, clients: clientCount() });
  port.start();
};
