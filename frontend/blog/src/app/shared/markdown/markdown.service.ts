import { Injectable, SecurityContext, inject } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { Marked, Renderer, type Tokens } from 'marked';

/** URL schemes allowed in Markdown links and images. */
const SAFE_PROTOCOLS = new Set(['http:', 'https:', 'mailto:']);

function escapeHtml(value: string): string {
  return value
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}

/**
 * Returns the URL if it uses a safe scheme (or is relative), `null` otherwise.
 */
function safeUrl(raw: string | null | undefined): string | null {
  if (!raw) {
    return null;
  }

  const trimmed = raw.trim();

  // Relative URLs (paths, anchors) are always fine.
  if (!/^[a-z][a-z0-9+.-]*:/i.test(trimmed)) {
    return trimmed;
  }

  let protocol: string;
  try {
    protocol = new URL(trimmed).protocol.toLowerCase();
  } catch {
    return null;
  }

  return SAFE_PROTOCOLS.has(protocol) ? trimmed : null;
}

/**
 * Renders untrusted Markdown to sanitized HTML.
 *
 * Defense in depth:
 * 1. A custom `marked` renderer escapes every raw HTML token, so Markdown can
 *    never smuggle `<script>`, event handlers, `<iframe>`, etc.
 * 2. Link/image URLs are restricted to http(s)/mailto (plus relative URLs),
 *    and external links get `rel="noopener noreferrer"`.
 * 3. The final HTML passes through Angular's DomSanitizer before being bound
 *    with `[innerHTML]`, which sanitizes again. No security context is ever
 *    bypassed.
 */
@Injectable({ providedIn: 'root' })
export class MarkdownService {
  private readonly sanitizer = inject(DomSanitizer);

  private readonly marked = this.createMarked();

  /** Renders Markdown to a string that is safe to bind with `[innerHTML]`. */
  render(markdown: string): string {
    if (!markdown.trim()) {
      return '';
    }

    const html = this.marked.parse(markdown, { async: false });

    return this.sanitizer.sanitize(SecurityContext.HTML, html) ?? '';
  }

  private createMarked(): Marked {
    const renderer = new Renderer();

    // Never pass raw HTML from Markdown through to the output.
    renderer.html = ({ text }: Tokens.HTML | Tokens.Tag) => escapeHtml(text);

    // A regular function is required so marked binds `this` to the renderer
    // (which exposes `parser` for rendering the link label tokens).
    renderer.link = function ({ href, title, tokens }: Tokens.Link): string {
      const label = this.parser.parseInline(tokens);
      const url = safeUrl(href);

      if (!url) {
        return label;
      }

      const titleAttr = title ? ` title="${escapeHtml(title)}"` : '';
      const isExternal = /^https?:/i.test(url);
      const rel = isExternal ? ' target="_blank" rel="noopener noreferrer"' : '';

      return `<a href="${escapeHtml(url)}"${titleAttr}${rel}>${label}</a>`;
    };

    renderer.image = ({ href, title, text }: Tokens.Image): string => {
      const url = safeUrl(href);

      if (!url) {
        return text ? escapeHtml(text) : '';
      }

      const titleAttr = title ? ` title="${escapeHtml(title)}"` : '';

      return `<img src="${escapeHtml(url)}" alt="${escapeHtml(text ?? '')}"${titleAttr} loading="lazy" />`;
    };

    // Code blocks render as escaped text inside <pre><code>, never HTML.
    renderer.code = ({ text, lang }: Tokens.Code): string => {
      const langClass = lang ? ` class="language-${escapeHtml(lang.trim().split(/\s+/)[0] ?? '')}"` : '';

      return `<pre><code${langClass}>${escapeHtml(text)}</code></pre>`;
    };

    return new Marked({ gfm: true, breaks: false, renderer });
  }
}
