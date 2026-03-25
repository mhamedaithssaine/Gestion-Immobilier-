import { Component, effect, HostListener, inject } from '@angular/core';
import { Router } from '@angular/router';
import { SpaceModalService } from '../../services/space-modal.service';

@Component({
  selector: 'app-skna-space-modal',
  standalone: true,
  templateUrl: './skna-space-modal.component.html',
  styleUrl: './skna-space-modal.component.scss'
})
export class SknaSpaceModalComponent {
  readonly spaceModal = inject(SpaceModalService);
  private readonly router = inject(Router);

  constructor() {
    effect((onCleanup) => {
      document.body.style.overflow = this.spaceModal.isOpen() ? 'hidden' : '';
      onCleanup(() => {
        document.body.style.overflow = '';
      });
    });
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.spaceModal.isOpen()) {
      this.close();
    }
  }

  close(): void {
    this.spaceModal.close();
  }

  goClientLogin(): void {
    this.spaceModal.close();
    void this.router.navigate(['/login']);
  }

  goCandidateLogin(): void {
    this.spaceModal.close();
    void this.router.navigate(['/login']);
  }
}
