import { Component, inject, signal } from '@angular/core';
import {
  email,
  form,
  FormField,
  FormRoot,
  minLength,
  required,
} from '@angular/forms/signals';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-register',
  imports: [FormField, FormRoot, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register { }