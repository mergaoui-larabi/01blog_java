import { describe, it, expect } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { MarkdownService } from './markdown.service';

describe('MarkdownService', () => {
  let markdown: MarkdownService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MarkdownService],
    });
    markdown = TestBed.inject(MarkdownService);
  });

  it('renders headings, bold and lists', () => {
    const html = markdown.render('# Title\n\n**bold** and *italic*\n\n- one\n- two\n\n> blockquote');
    expect(html).toContain('<h1');
    expect(html).toContain('<strong>bold</strong>');
    expect(html).toContain('<li>one</li>');
    expect(html).toContain('<blockquote>');
  });

  it('escapes raw HTML so scripts cannot execute', () => {
    const html = markdown.render('<script>alert(1)</script><img src=x onerror=alert(1)>');
    expect(html).not.toContain('<script');
    expect(html).not.toContain('<script');
    expect(html).toContain('&lt;script&gt;alert(1)&lt;/script&gt;');
  });

  it('sanitizes unsafe link schemes', () => {
    const html = markdown.render('[x](javascript:alert(1))');
    expect(html).not.toContain('javascript:');
  });

  it('renders external links with a safe target and rel', () => {
    const html = markdown.render('[ok](https://example.com/page?x=1)');
    expect(html).toContain('target="_blank"');
    expect(html).toContain('rel="noopener noreferrer"');
  });

  it('renders fenced code blocks as code', () => {
    const html = markdown.render('```ts\nconst x = 1;\n```');
    expect(html).toContain('<code');
    expect(html).toContain('language-ts');
  });

  it('renders fenced code blocks as code', () => {
    const html = markdown.render('```ts\nconst x = 1;\n```');
    expect(html).toContain('<code');
    expect(html).toContain('language-ts');
  });
  it('sanitizes image URLs', () => {
    const html = markdown.render('![img](data:image/png;base64,abc)');
    expect(html).not.toContain('data:');
  });

  it('returns empty string for blank content', () => {
    expect(markdown.render('  ')).toBe('');
  });
});
