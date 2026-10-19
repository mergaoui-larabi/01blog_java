import {
  Component,
  OnDestroy,
  computed,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { Post, PostAttachment } from '../../models/post.model';
import { PostsService } from '../../data-access/posts.service';
import { AuthService } from '../../../auth/state/auth.service';
import { MarkdownService } from '../../../../shared/markdown/markdown.service';

type EditorMode = 'write' | 'preview';

interface StagedFile {
  file: File;
  url: string;
  kind: 'IMAGE' | 'VIDEO';
}

const MAX_IMAGE_BYTES = 5 * 1024 * 1024;
const MAX_VIDEO_BYTES = 50 * 1024 * 1024;
const MAX_CONTENT_LENGTH = 20_000;

const ACCEPTED_IMAGE_TYPES = ['image/png', 'image/jpeg', 'image/gif', 'image/webp', 'image/svg+xml'];
const ACCEPTED_VIDEO_TYPES = ['video/mp4', 'video/webm', 'video/ogg'];

/**
 * Markdown post composer used for both creating and editing posts.
 *
 * Media files stay local: selection produces object-URL previews and the
 * staged files are handed to the parent on submit. Uploading to the media
 * microservice is a future concern — this component only needs to emit the
 * staged `File`s along with the text.
 */
@Component({
  selector: 'app-post-composer',
  templateUrl: './composer.html',
  imports: [DecimalPipe],
})
export class PostComposer implements OnDestroy {
  private readonly postsService = inject(PostsService);
  private readonly authService = inject(AuthService);
  private readonly markdownService = inject(MarkdownService);

  /** When set, the composer edits that post instead of creating one. */
  readonly editingPost = input<Post | null>(null);
  /** Heading text; defaults based on mode. */
  readonly title = input('Share an update');

  readonly submitted = output<Post>();
  readonly cancelled = output<void>();

  protected readonly mode = signal<EditorMode>('write');
  protected readonly content = signal('');
  protected readonly stagedFiles = signal<StagedFile[]>([]);
  protected readonly error = signal('');
  protected readonly submitting = signal(false);
  protected readonly fileError = signal('');

  protected readonly previewHtml = computed(() => this.markdownService.render(this.content()));
  protected readonly isEditing = computed(() => this.editingPost() !== null);
  protected readonly canSubmit = computed(
    () => this.content().trim().length > 0 && this.content().length <= MAX_CONTENT_LENGTH,
  );

  protected readonly maxContentLength = MAX_CONTENT_LENGTH;
  protected readonly acceptTypes = [...ACCEPTED_IMAGE_TYPES, ...ACCEPTED_VIDEO_TYPES].join(',');

  constructor() {
    const existing = this.editingPost();
    if (existing) {
      this.content.set(existing.content);
    }
  }

  ngOnDestroy(): void {
    this.revokeAll();
  }

  protected setMode(mode: EditorMode): void {
    this.mode.set(mode);
  }

  protected onFileSelected(event: Event): void {
    const inputEl = event.target as HTMLInputElement;
    const files = Array.from(inputEl.files ?? []);

    this.fileError.set('');

    for (const file of files) {
      const kind = this.validateFile(file);
      if (!kind) {
        continue;
      }

      this.stagedFiles.update((files) => [...files, { file, url: URL.createObjectURL(file), kind }]);
    }

    // Allow re-selecting the same file after removal.
    inputEl.value = '';
  }

  protected removeFile(index: number): void {
    const files = this.stagedFiles();
    const target = files[index];
    if (target) {
      URL.revokeObjectURL(target.url);
    }

    this.stagedFiles.update((files) => files.filter((_, i) => i !== index));
  }

  protected cancel(): void {
    this.revokeAll();
    this.stagedFiles.set([]);
    this.content.set('');
    this.error.set('');
    this.cancelled.emit();
  }

  protected submit(): void {
    this.error.set('');

    const content = this.content().trim();
    if (!content) {
      this.error.set('Post content cannot be empty.');
      return;
    }

    const username = this.authService.user()?.username;
    if (!username) {
      this.error.set('You need to be signed in to publish.');
      return;
    }

    this.submitting.set(true);

    // Mock persistence: no network request is made.
    setTimeout(() => {
      const attachments: PostAttachment[] = this.stagedFiles().map((staged, index) => ({
        id: `upload-${index}-${crypto.randomUUID()}`,
        kind: staged.kind,
        url: staged.url,
        mimeType: staged.file.type,
        fileName: staged.file.name,
        alt: staged.file.name,
      }));

      const existing = this.editingPost();

      if (existing) {
        this.postsService.updatePost(existing.id, { content, attachments });
        this.submitted.emit({ ...existing, content, attachments });
      } else {
        const post = this.postsService.createPost({ content, attachments }, username);
        this.content.set('');
        this.stagedFiles.set([]);
        this.submitted.emit(post);
      }

      this.submitting.set(false);
    }, 400);
  }

  /** Returns the attachment kind when the file is acceptable, else null. */
  private validateFile(file: File): 'IMAGE' | 'VIDEO' | null {
    if (ACCEPTED_IMAGE_TYPES.includes(file.type)) {
      if (file.size > MAX_IMAGE_BYTES) {
        this.fileError.set(`"${file.name}" is larger than the 5 MB image limit.`);
        return null;
      }
      return 'IMAGE';
    }

    if (ACCEPTED_VIDEO_TYPES.includes(file.type)) {
      if (file.size > MAX_VIDEO_BYTES) {
        this.fileError.set(`"${file.name}" is larger than the 50 MB video limit.`);
        return null;
      }
      return 'VIDEO';
    }

    this.fileError.set(`"${file.name}" is not a supported image or video file.`);
    return null;
  }

  private revokeAll(): void {
    for (const staged of this.stagedFiles()) {
      URL.revokeObjectURL(staged.url);
    }
  }
}
