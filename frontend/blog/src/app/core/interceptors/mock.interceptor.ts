import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { of, delay } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';

/* ------------------------------------------------------------------ */
/*  Mock Data                                                          */
/* ------------------------------------------------------------------ */

interface MockUser {
  id: number;
  username: string;
  email: string;
  bio: string;
  avatarUrl: string;
  role: 'USER' | 'ADMIN';
  createdAt: string;
  subscriberCount: number;
  postCount: number;
  isSubscribed?: boolean;
  isBanned?: boolean;
}

interface MockPost {
  id: number;
  title: string;
  content: string;
  mediaUrl?: string;
  mediaType?: 'image' | 'video';
  author: MockUser;
  createdAt: string;
  updatedAt: string;
  likeCount: number;
  commentCount: number;
  isLiked?: boolean;
}

interface MockComment {
  id: number;
  content: string;
  author: MockUser;
  postId: number;
  createdAt: string;
}

interface MockNotification {
  id: number;
  type: 'NEW_POST' | 'SUBSCRIBE' | 'LIKE' | 'COMMENT';
  message: string;
  isRead: boolean;
  createdAt: string;
  relatedUserId?: number;
  relatedPostId?: number;
  relatedUsername?: string;
}

interface MockReport {
  id: number;
  reportedUser: MockUser;
  reportedBy: MockUser;
  reason: string;
  createdAt: string;
  status: 'PENDING' | 'RESOLVED' | 'DISMISSED';
}

/* ---- Users ---- */

const MOCK_USERS: MockUser[] = [
  {
    id: 1,
    username: 'johndoe',
    email: 'john@example.com',
    bio: 'Full-stack developer passionate about Spring Boot and Angular. Currently learning advanced Java concepts.',
    avatarUrl: 'https://api.dicebear.com/7.x/avataaars/svg?seed=johndoe',
    role: 'ADMIN',
    createdAt: '2025-09-01T08:00:00Z',
    subscriberCount: 42,
    postCount: 8,
  },
  {
    id: 2,
    username: 'janesmith',
    email: 'jane@example.com',
    bio: 'Frontend enthusiast exploring the world of web development. Love React, Angular, and CSS art!',
    avatarUrl: 'https://api.dicebear.com/7.x/avataaars/svg?seed=janesmith',
    role: 'USER',
    createdAt: '2025-10-15T10:30:00Z',
    subscriberCount: 18,
    postCount: 5,
  },
  {
    id: 3,
    username: 'bobwilson',
    email: 'bob@example.com',
    bio: 'Backend developer diving deep into microservices, Docker, and cloud deployment.',
    avatarUrl: 'https://api.dicebear.com/7.x/avataaars/svg?seed=bobwilson',
    role: 'USER',
    createdAt: '2025-11-20T14:00:00Z',
    subscriberCount: 7,
    postCount: 3,
  },
];

/* ---- Posts ---- */

const NOW = Date.now();
const DAY = 86_400_000;

const MOCK_POSTS: MockPost[] = [
  {
    id: 1,
    title: 'Getting Started with Spring Boot: My First REST API',
    content: `After spending the last two weeks learning Spring Boot, I finally built my first REST API! Here are some key takeaways:\n\n**1. Start with Spring Initializr**\nThe easiest way to bootstrap a project. Just pick your dependencies and generate.\n\n**2. Understand the layers**\nController → Service → Repository pattern makes the code so clean and maintainable.\n\n**3. Don't skip testing**\nWriting unit tests early saved me hours of debugging later.\n\nI've uploaded the full project on GitHub if anyone wants to check it out. Would love feedback!`,
    mediaUrl: 'https://picsum.photos/seed/springboot/800/400',
    mediaType: 'image',
    author: MOCK_USERS[0],
    createdAt: new Date(NOW - 1 * DAY).toISOString(),
    updatedAt: new Date(NOW - 1 * DAY).toISOString(),
    likeCount: 24,
    commentCount: 5,
    isLiked: false,
  },
  {
    id: 2,
    title: 'CSS Grid vs Flexbox: When to Use What',
    content: `I used to get confused about when to use Grid vs Flexbox. Here's my simple rule:\n\n**Flexbox = One-dimensional**\nUse it for navigation bars, centering items, or laying out items in a row or column.\n\n**Grid = Two-dimensional**\nUse it for complex page layouts, card grids, or any time you need both rows AND columns.\n\nBoth are incredibly powerful. Once you master both, you'll rarely need a CSS framework for layouts!`,
    mediaUrl: 'https://picsum.photos/seed/cssgrid/800/400',
    mediaType: 'image',
    author: MOCK_USERS[1],
    createdAt: new Date(NOW - 2 * DAY).toISOString(),
    updatedAt: new Date(NOW - 2 * DAY).toISOString(),
    likeCount: 31,
    commentCount: 8,
    isLiked: false,
  },
  {
    id: 3,
    title: 'Docker Compose for Local Development: A Quick Guide',
    content: `Setting up Docker Compose transformed my development workflow. Here's what I learned:\n\n**Why Docker Compose?**\nNo more "it works on my machine" — everyone on the team gets the exact same environment.\n\n**My setup**\n- PostgreSQL + pgAdmin\n- Redis for caching\n- The app itself\n- All with one \`docker-compose up\` command\n\nPro tip: Use health checks to ensure your database is ready before the app starts.`,
    mediaUrl: 'https://picsum.photos/seed/docker/800/400',
    mediaType: 'image',
    author: MOCK_USERS[2],
    createdAt: new Date(NOW - 3 * DAY).toISOString(),
    updatedAt: new Date(NOW - 3 * DAY).toISOString(),
    likeCount: 15,
    commentCount: 3,
    isLiked: false,
  },
  {
    id: 4,
    title: 'Understanding JWT Authentication in Angular',
    content: `After implementing JWT auth in Angular, I wanted to share the architecture I settled on:\n\n**The Flow**\n1. Login → receive JWT token from backend\n2. Store token in localStorage\n3. Attach via HttpInterceptor to every request\n4. Route guards check auth state\n5. On 401 → redirect to login\n\n**Security note**: Always store sensitive tokens securely, and implement refresh token rotation for production apps.\n\nI'll share the code in my next post!`,
    mediaUrl: 'https://picsum.photos/seed/jwt/800/400',
    mediaType: 'image',
    author: MOCK_USERS[0],
    createdAt: new Date(NOW - 4 * DAY).toISOString(),
    updatedAt: new Date(NOW - 4 * DAY).toISOString(),
    likeCount: 19,
    commentCount: 6,
    isLiked: false,
  },
  {
    id: 5,
    title: 'My First Open Source Contribution!',
    content: `I finally did it! Made my first PR to an open-source project. Here's the experience:\n\n**Finding the project**\nStarted with "good first issue" labels on GitHub. Found a documentation project that was beginner-friendly.\n\n**The process**\n- Forked the repo\n- Made my changes\n- Submitted the PR\n- Got reviewed (and some great feedback!)\n- Merged! 🎉\n\nIt's a small contribution, but I'm proud of it. Everyone should try contributing to open source at least once.`,
    mediaUrl: 'https://picsum.photos/seed/opensource/800/400',
    mediaType: 'image',
    author: MOCK_USERS[1],
    createdAt: new Date(NOW - 5 * DAY).toISOString(),
    updatedAt: new Date(NOW - 5 * DAY).toISOString(),
    likeCount: 45,
    commentCount: 12,
    isLiked: false,
  },
  {
    id: 6,
    title: 'Deploying a Spring Boot App to Railway: Step by Step',
    content: `Deploying can be intimidating, but Railway makes it surprisingly painless. Here's my exact setup:\n\n1. Push your code to GitHub\n2. Connect Railway to your repo\n3. Add a \`Dockerfile\` or let Railway auto-detect\n4. Set environment variables\n5. Deploy!\n\n**Cost**: Absolutely free for small projects. Perfect for student portfolios.\n\n**Gotchas**:\n- Make sure your port is configurable via \`PORT\` env variable\n- Use \`SPRING_PROFILES_ACTIVE=prod\` for production settings`,
    mediaUrl: 'https://picsum.photos/seed/deploy/800/400',
    mediaType: 'image',
    author: MOCK_USERS[0],
    createdAt: new Date(NOW - 6 * DAY).toISOString(),
    updatedAt: new Date(NOW - 6 * DAY).toISOString(),
    likeCount: 28,
    commentCount: 4,
    isLiked: false,
  },
];

/* ---- Comments ---- */

const MOCK_COMMENTS: MockComment[] = [
  { id: 1, content: 'Great post! I just started learning Spring Boot too. Would love to see your GitHub repo!', author: MOCK_USERS[1], postId: 1, createdAt: new Date(NOW - 1 * DAY + 3600_000).toISOString() },
  { id: 2, content: 'Thanks! Adding the link to the post now. Happy to collaborate 🤝', author: MOCK_USERS[0], postId: 1, createdAt: new Date(NOW - 1 * DAY + 7200_000).toISOString() },
  { id: 3, content: 'Spring Boot is awesome. Try adding Spring Security next — it\'s a game changer.', author: MOCK_USERS[2], postId: 1, createdAt: new Date(NOW - 1 * DAY + 10800_000).toISOString() },
  { id: 4, content: 'This is so helpful! I always reached for Grid first, but now I understand the distinction.', author: MOCK_USERS[0], postId: 2, createdAt: new Date(NOW - 2 * DAY + 3600_000).toISOString() },
  { id: 5, content: 'I use Flexbox for 90% of things and Grid for the remaining 10%. Simple mental model!', author: MOCK_USERS[2], postId: 2, createdAt: new Date(NOW - 2 * DAY + 7200_000).toISOString() },
  { id: 6, content: 'Thanks for the Docker guide! Health checks are a lifesaver.', author: MOCK_USERS[0], postId: 3, createdAt: new Date(NOW - 3 * DAY + 3600_000).toISOString() },
  { id: 7, content: 'Can you share your docker-compose.yml? I want to compare with mine.', author: MOCK_USERS[1], postId: 3, createdAt: new Date(NOW - 3 * DAY + 7200_000).toISOString() },
  { id: 8, content: 'Congrats on the first PR! 🎉 Such a great feeling.', author: MOCK_USERS[0], postId: 5, createdAt: new Date(NOW - 5 * DAY + 3600_000).toISOString() },
  { id: 9, content: 'Thank you! It really is. Everyone should try it at least once.', author: MOCK_USERS[1], postId: 5, createdAt: new Date(NOW - 5 * DAY + 7200_000).toISOString() },
  { id: 10, content: 'Railway looks promising! How does it compare to Heroku?', author: MOCK_USERS[1], postId: 6, createdAt: new Date(NOW - 6 * DAY + 3600_000).toISOString() },
  { id: 11, content: 'Much simpler setup, and the free tier is more generous. Highly recommend!', author: MOCK_USERS[0], postId: 6, createdAt: new Date(NOW - 6 * DAY + 7200_000).toISOString() },
];

/* ---- Notifications ---- */

const MOCK_NOTIFICATIONS: MockNotification[] = [
  { id: 1, type: 'LIKE', message: 'janesmith liked your post "Getting Started with Spring Boot"', isRead: false, createdAt: new Date(NOW - 2 * DAY).toISOString(), relatedUserId: 2, relatedPostId: 1, relatedUsername: 'janesmith' },
  { id: 2, type: 'COMMENT', message: 'bobwilson commented on your post "Getting Started with Spring Boot"', isRead: false, createdAt: new Date(NOW - 1 * DAY + 10800_000).toISOString(), relatedUserId: 3, relatedPostId: 1, relatedUsername: 'bobwilson' },
  { id: 3, type: 'SUBSCRIBE', message: 'janesmith started following you', isRead: true, createdAt: new Date(NOW - 7 * DAY).toISOString(), relatedUserId: 2, relatedUsername: 'janesmith' },
  { id: 4, type: 'NEW_POST', message: 'janesmith published a new post: "My First Open Source Contribution!"', isRead: false, createdAt: new Date(NOW - 5 * DAY).toISOString(), relatedUserId: 2, relatedPostId: 5, relatedUsername: 'janesmith' },
  { id: 5, type: 'LIKE', message: 'bobwilson liked your post "Understanding JWT Authentication"', isRead: true, createdAt: new Date(NOW - 3 * DAY).toISOString(), relatedUserId: 3, relatedPostId: 4, relatedUsername: 'bobwilson' },
  { id: 6, type: 'COMMENT', message: 'janesmith commented on your post "Deploying Spring Boot to Railway"', isRead: false, createdAt: new Date(NOW - 6 * DAY + 3600_000).toISOString(), relatedUserId: 2, relatedPostId: 6, relatedUsername: 'janesmith' },
];

/* ---- Reports ---- */

const MOCK_REPORTS: MockReport[] = [
  { id: 1, reportedUser: MOCK_USERS[1], reportedBy: MOCK_USERS[0], reason: 'Inappropriate content in recent post comments', createdAt: new Date(NOW - 4 * DAY).toISOString(), status: 'PENDING' as const },
  { id: 2, reportedUser: MOCK_USERS[2], reportedBy: MOCK_USERS[1], reason: 'Spamming irrelevant links in comment sections', createdAt: new Date(NOW - 3 * DAY).toISOString(), status: 'RESOLVED' as const },
  { id: 3, reportedUser: MOCK_USERS[0], reportedBy: MOCK_USERS[2], reason: 'Harassment in direct messages', createdAt: new Date(NOW - 2 * DAY).toISOString(), status: 'PENDING' as const },
];

/* ---- Helpers ---- */

function id<T extends { id: number }>(items: T[]): number {
  return Math.max(...items.map((i) => i.id), 0) + 1;
}

function cloneAndSortPosts(posts: MockPost[]): MockPost[] {
  return [...posts].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
}

/* ---- Interceptor ---- */

export const mockInterceptor: HttpInterceptorFn = (req, next) => {
  const { method, url } = req;
  const platformId = inject(PLATFORM_ID);

  // Only intercept in the browser (not during SSR)
  if (!isPlatformBrowser(platformId)) {
    return next(req);
  }

  const path = url.replace(/^https?:\/\/[^/]+/, '');

  /* ---- Auth ---- */

  if (path === '/api/auth/login' && method === 'POST') {
    const body = req.body as { username?: string; password?: string };
    const username = body?.username || 'guest';
    const user = MOCK_USERS.find((u) => u.username === username) || { ...MOCK_USERS[0], id: 4, username, role: 'USER' as const };
    const token = `mock-jwt-${user.id}-${Date.now()}`;
    return of(
      new HttpResponse({
        status: 200,
        body: { token, user },
      }),
    ).pipe(delay(600));
  }

  if (path === '/api/auth/register' && method === 'POST') {
    const body = req.body as { username?: string; email?: string; password?: string };
    const user: MockUser = {
      id: id(MOCK_USERS),
      username: body?.username || 'newuser',
      email: body?.email || 'new@example.com',
      bio: 'Hello, I\'m new here!',
      avatarUrl: `https://api.dicebear.com/7.x/avataaars/svg?seed=${body?.username || 'new'}`,
      role: 'USER',
      createdAt: new Date().toISOString(),
      subscriberCount: 0,
      postCount: 0,
    };
    const token = `mock-jwt-${user.id}-${Date.now()}`;
    return of(
      new HttpResponse({
        status: 201,
        body: { token, user },
      }),
    ).pipe(delay(600));
  }

  /* ---- Posts ---- */

  if (path === '/api/posts/feed' && method === 'GET') {
    return of(
      new HttpResponse({
        status: 200,
        body: cloneAndSortPosts(MOCK_POSTS),
      }),
    ).pipe(delay(300));
  }

  const postMatch = path.match(/^\/api\/posts\/(\d+)$/);
  if (postMatch && method === 'GET') {
    const id = Number(postMatch[1]);
    const post = MOCK_POSTS.find((p) => p.id === id);
    if (post) {
      return of(new HttpResponse({ status: 200, body: post })).pipe(delay(200));
    }
    return of(new HttpResponse({ status: 404, body: { message: 'Post not found' } }));
  }

  if (postMatch && method === 'DELETE') {
    return of(new HttpResponse({ status: 200, body: {} })).pipe(delay(200));
  }

  if (path === '/api/posts' && method === 'POST') {
    const formData = req.body as FormData;
    const newPost: MockPost = {
      id: id(MOCK_POSTS),
      title: formData.get('title') as string || 'Untitled',
      content: formData.get('content') as string || '',
      mediaUrl: 'https://picsum.photos/seed/newpost/800/400',
      mediaType: 'image',
      author: { ...MOCK_USERS[0] },
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      likeCount: 0,
      commentCount: 0,
      isLiked: false,
    };
    MOCK_POSTS.push(newPost);
    return of(new HttpResponse({ status: 201, body: newPost })).pipe(delay(400));
  }

  const postUpdateMatch = path.match(/^\/api\/posts\/(\d+)$/);
  if (postUpdateMatch && method === 'PUT') {
    const id = Number(postUpdateMatch[1]);
    const formData = req.body as FormData;
    const post = MOCK_POSTS.find((p) => p.id === id);
    if (post) {
      post.title = (formData.get('title') as string) || post.title;
      post.content = (formData.get('content') as string) || post.content;
      post.updatedAt = new Date().toISOString();
      return of(new HttpResponse({ status: 200, body: post })).pipe(delay(300));
    }
    return of(new HttpResponse({ status: 404, body: { message: 'Post not found' } }));
  }

  const likeMatch = path.match(/^\/api\/posts\/(\d+)\/like$/);
  if (likeMatch) {
    const id = Number(likeMatch[1]);
    const post = MOCK_POSTS.find((p) => p.id === id);
    if (post) {
      if (method === 'POST') {
        if (post.isLiked) {
          return of(new HttpResponse({ status: 200, body: {} }));
        }
        post.isLiked = true;
        post.likeCount += 1;
        return of(new HttpResponse({ status: 200, body: {} })).pipe(delay(150));
      }
      if (method === 'DELETE') {
        if (!post.isLiked) {
          return of(new HttpResponse({ status: 200, body: {} }));
        }
        post.isLiked = false;
        post.likeCount = Math.max(0, post.likeCount - 1);
        return of(new HttpResponse({ status: 200, body: {} })).pipe(delay(150));
      }
    }
    return of(new HttpResponse({ status: 404, body: { message: 'Post not found' } }));
  }

  /* ---- Comments ---- */

  const commentsMatch = path.match(/^\/api\/posts\/(\d+)\/comments$/);
  if (commentsMatch) {
    const postId = Number(commentsMatch[1]);
    if (method === 'GET') {
      const comments = MOCK_COMMENTS.filter((c) => c.postId === postId);
      return of(new HttpResponse({ status: 200, body: comments })).pipe(delay(200));
    }
    if (method === 'POST') {
      const body = req.body as { content?: string };
      const newComment: MockComment = {
        id: id(MOCK_COMMENTS),
        content: body?.content || '',
        author: MOCK_USERS[0],
        postId,
        createdAt: new Date().toISOString(),
      };
      MOCK_COMMENTS.push(newComment);
      const post = MOCK_POSTS.find((p) => p.id === postId);
      if (post) post.commentCount += 1;
      return of(new HttpResponse({ status: 201, body: newComment })).pipe(delay(200));
    }
  }

  /* ---- Users ---- */

  const userMatch = path.match(/^\/api\/users\/([^/]+)$/);
  if (userMatch && method === 'GET') {
    const username = userMatch[1];
    const user = MOCK_USERS.find((u) => u.username === username);
    if (user) {
      return of(new HttpResponse({ status: 200, body: user })).pipe(delay(200));
    }
    return of(new HttpResponse({ status: 404, body: { message: 'User not found' } }));
  }

  if (userMatch && method === 'PUT') {
    const username = userMatch[1];
    const user = MOCK_USERS.find((u) => u.username === username);
    if (user) {
      const body = req.body as Partial<MockUser>;
      Object.assign(user, body);
      return of(new HttpResponse({ status: 200, body: user })).pipe(delay(200));
    }
    return of(new HttpResponse({ status: 404, body: { message: 'User not found' } }));
  }

  const userPostsMatch = path.match(/^\/api\/users\/([^/]+)\/posts$/);
  if (userPostsMatch && method === 'GET') {
    const username = userPostsMatch[1];
    const user = MOCK_USERS.find((u) => u.username === username);
    if (user) {
      const posts = MOCK_POSTS.filter((p) => p.author.username === username);
      return of(new HttpResponse({ status: 200, body: posts })).pipe(delay(200));
    }
    return of(new HttpResponse({ status: 404, body: { message: 'User not found' } }));
  }

  if (path === '/api/users/search' && method === 'GET') {
    const query = (req.params.get('q') || '').toLowerCase();
    const results = MOCK_USERS.filter((u) =>
      u.username.toLowerCase().includes(query) || u.bio.toLowerCase().includes(query),
    );
    return of(new HttpResponse({ status: 200, body: results })).pipe(delay(200));
  }

  const subscribeMatch = path.match(/^\/api\/users\/([^/]+)\/subscribe$/);
  if (subscribeMatch) {
    return of(new HttpResponse({ status: 200, body: {} })).pipe(delay(150));
  }

  /* ---- Notifications ---- */

  if (path === '/api/notifications' && method === 'GET') {
    return of(new HttpResponse({ status: 200, body: MOCK_NOTIFICATIONS })).pipe(delay(200));
  }

  if (path === '/api/notifications/unread-count' && method === 'GET') {
    const count = MOCK_NOTIFICATIONS.filter((n) => !n.isRead).length;
    return of(new HttpResponse({ status: 200, body: count })).pipe(delay(100));
  }

  if (path === '/api/notifications/read-all' && method === 'PUT') {
    MOCK_NOTIFICATIONS.forEach((n) => (n.isRead = true));
    return of(new HttpResponse({ status: 200, body: {} })).pipe(delay(200));
  }

  const notifReadMatch = path.match(/^\/api\/notifications\/(\d+)\/read$/);
  if (notifReadMatch && method === 'PUT') {
    const id = Number(notifReadMatch[1]);
    const notif = MOCK_NOTIFICATIONS.find((n) => n.id === id);
    if (notif) notif.isRead = true;
    return of(new HttpResponse({ status: 200, body: {} })).pipe(delay(100));
  }

  const commentsDeleteMatch = path.match(/^\/api\/posts\/(\d+)\/comments\/(\d+)$/);
  if (commentsDeleteMatch && method === 'DELETE') {
    const postId = Number(commentsDeleteMatch[1]);
    const commentId = Number(commentsDeleteMatch[2]);
    const idx = MOCK_COMMENTS.findIndex((c) => c.id === commentId && c.postId === postId);
    if (idx !== -1) {
      MOCK_COMMENTS.splice(idx, 1);
      const post = MOCK_POSTS.find((p) => p.id === postId);
      if (post) post.commentCount = Math.max(0, post.commentCount - 1);
      return of(new HttpResponse({ status: 200, body: {} })).pipe(delay(150));
    }
    return of(new HttpResponse({ status: 404, body: { message: 'Comment not found' } }));
  }

  /* ---- Reports ---- */

  if (path === '/api/reports' && method === 'POST') {
    const body = req.body as { reportedUserId?: number; reason?: string };
    const newReport: MockReport = {
      id: id(MOCK_REPORTS),
      reportedUser: MOCK_USERS.find((u) => u.id === body?.reportedUserId) || MOCK_USERS[1],
      reportedBy: MOCK_USERS[0],
      reason: body?.reason || 'No reason provided',
      createdAt: new Date().toISOString(),
      status: 'PENDING',
    };
    MOCK_REPORTS.push(newReport);
    return of(new HttpResponse({ status: 201, body: newReport })).pipe(delay(400));
  }

  /* ---- Admin ---- */

  if (path === '/api/admin/users' && method === 'GET') {
    return of(new HttpResponse({ status: 200, body: MOCK_USERS })).pipe(delay(300));
  }

  if (path === '/api/admin/posts' && method === 'GET') {
    return of(new HttpResponse({ status: 200, body: MOCK_POSTS })).pipe(delay(300));
  }

  if (path === '/api/admin/reports' && method === 'GET') {
    return of(new HttpResponse({ status: 200, body: MOCK_REPORTS })).pipe(delay(300));
  }

  const adminUserDelete = path.match(/^\/api\/admin\/users\/(\d+)$/);
  if (adminUserDelete && method === 'DELETE') {
    return of(new HttpResponse({ status: 200, body: {} })).pipe(delay(200));
  }

  const adminUserBan = path.match(/^\/api\/admin\/users\/(\d+)\/(ban|unban)$/);
  if (adminUserBan && method === 'POST') {
    return of(new HttpResponse({ status: 200, body: {} })).pipe(delay(200));
  }

  const adminPostDelete = path.match(/^\/api\/admin\/posts\/(\d+)$/);
  if (adminPostDelete && method === 'DELETE') {
    return of(new HttpResponse({ status: 200, body: {} })).pipe(delay(200));
  }

  const adminReportAction = path.match(/^\/api\/admin\/reports\/(\d+)\/(resolve|dismiss)$/);
  if (adminReportAction && method === 'POST') {
    return of(new HttpResponse({ status: 200, body: {} })).pipe(delay(200));
  }

  // Pass through to the next handler (or real HTTP call)
  return next(req);
};
