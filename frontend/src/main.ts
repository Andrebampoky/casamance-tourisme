import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient } from '@angular/common/http';
import { LOCALE_ID } from '@angular/core';
import { registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';
import { AppComponent } from './app/app.component';

registerLocaleData(localeFr, 'fr');
bootstrapApplication(AppComponent, { providers: [provideHttpClient(), { provide: LOCALE_ID, useValue: 'fr' }] })
  .catch((error) => console.error(error));
