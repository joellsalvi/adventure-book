import {NgModule} from '@angular/core';
import {AppComponent} from "./component/app/app.component";
import {provideRouter, RouterModule} from "@angular/router";
import {BrowserModule} from "@angular/platform-browser";
import {CommonModule} from "@angular/common";
import {provideHttpClient, withFetch} from "@angular/common/http";
import {provideAnimationsAsync} from '@angular/platform-browser/animations/async';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatSelectModule} from '@angular/material/select';
import {routes} from "./app.routes";
import {BookSelectorComponent} from './component/book-selector/book-selector.component';
import {ConsequenceMessageComponent} from './component/consequence-message/consequence-message.component';
import {GameStatusComponent} from './component/game-status/game-status.component';
import {HealthBarComponent} from './component/health-bar/health-bar.component';
import {SectionViewComponent} from './component/section-view/section-view.component';

export const GAME_COMPONENTS = [
  AppComponent,
  BookSelectorComponent,
  ConsequenceMessageComponent,
  GameStatusComponent,
  HealthBarComponent,
  SectionViewComponent,
];

export const MATERIAL_MODULES = [
  MatButtonModule,
  MatFormFieldModule,
  MatProgressBarModule,
  MatSelectModule,
];

@NgModule({
  declarations: GAME_COMPONENTS,
  imports: [
    BrowserModule,
    CommonModule,
    RouterModule,
    ...MATERIAL_MODULES,
  ],
  providers: [
    provideHttpClient(withFetch()),
    provideAnimationsAsync(),
    provideRouter(routes)
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
