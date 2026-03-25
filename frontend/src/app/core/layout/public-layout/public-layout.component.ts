import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SknaFooterComponent } from '../skna-footer/skna-footer.component';
import { SknaHeaderComponent } from '../skna-header/skna-header.component';
import { SknaSpaceModalComponent } from '../skna-space-modal/skna-space-modal.component';

@Component({
  selector: 'app-public-layout',
  standalone: true,
  imports: [RouterOutlet, SknaHeaderComponent, SknaFooterComponent, SknaSpaceModalComponent],
  templateUrl: './public-layout.component.html',
  styleUrl: './public-layout.component.scss'
})
export class PublicLayoutComponent {}
