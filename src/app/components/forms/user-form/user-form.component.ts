import { CommonModule } from '@angular/common';
import {
  Component,
  EventEmitter,
  inject,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import { MessageService, ConfirmationService } from 'primeng/api';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import { Button } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { Message } from 'primeng/message';
import { MultiSelect } from 'primeng/multiselect';
import { Password } from 'primeng/password';
import { Role, User } from '../../../model/auth/user.model';
import { UserRequest } from '../../../model/auth/request/user.request';
import { HttpErrorResponse } from '@angular/common/http';
import { UserService } from '../../../services/auth/user.service';
import { RoleRequest } from '../../../model/auth/request/role.request';
import { ErrorDetail } from '../../../model/error-response.model';

@Component({
  selector: 'app-user-form',
  templateUrl: './user-form.component.html',
  styleUrls: ['../form.component.scss', '../selector.component.scss'],
  imports: [CommonModule, FormsModule, ReactiveFormsModule, InputTextModule, Message, Button, Password, MultiSelect],
  standalone: true
})
export class UserFormComponent implements OnInit, OnChanges {
  @Input() user: User | null = null;
  @Input() currentUser?: User;

  @Output() userSubmit = new EventEmitter<User>();
  @Output() cancel = new EventEmitter<void>();
  @Output() changesOccurred = new EventEmitter<boolean>();

  allRoles = Object.entries(Role).map(([key, value]) => ({
    label: key,
    value: value
  }));

  isLoading = false;
  userForm: FormGroup;

  private messageService = inject(MessageService);
  private confirmationService = inject(ConfirmationService);
  private fb = inject(FormBuilder);
  private userService = inject(UserService);

  constructor() {
    this.userForm = this.createForm();
  }

  get submitButtonText(): string {
    return 'Обновить';
  }

  get submitButtonIcon(): string {
    return 'pi pi-pencil';
  }

  ngOnInit(): void {
    this.updateFormState();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['user']) {
      this.updateFormState();
    }
  }

  isInvalid(fieldName: string): boolean {
    const field = this.userForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  getFieldError(fieldName: string): string {
    const field = this.userForm.get(fieldName);
    if (!field || !field.errors) return '';
    if (field.errors['required']) return 'Это поле обязательно';
    return 'Недопустимое значение';
  }

  onSubmit(): void {
    this.markFormAsTouched();

    if (this.userForm.invalid) {
      this.showValidationError();
      return;
    }

    if (this.user) {
      this.confirmationService.confirm({
        message: `Вы действительно хотите изменить данные пользователя "${this.user.username}"?`,
        header: 'Подтверждение изменений',
        acceptLabel: 'Обновить',
        rejectLabel: 'Нет',
        acceptButtonProps: { severity: 'danger' },
        rejectButtonProps: { severity: 'secondary', outlined: true },
        accept: () => {
          if (this.user) {
            this.submitForm(this.user);
          }
        },
        reject: () => {
        }
      });
    }
  }

  onCancel(): void {
    const hasChanges = this.userForm.dirty && this.userForm.touched;
    this.changesOccurred.emit(hasChanges);
    this.cancel.emit();
  }

  private createForm(): FormGroup {
    return this.fb.group({
      username: ['', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(50),
      ]],
      password: ['', [
        Validators.minLength(6),
      ]],
      roles: [[]]
    });
  }

  private updateFormState(): void {
    if (this.user) {
      this.userForm.patchValue({
        username: this.user.username,
        roles: [...this.user.roles]
      });
      this.userForm.get('password')?.setValue('');
    } else {
      this.userForm.reset();
    }
  }

  private markFormAsTouched(): void {
    this.userForm.markAllAsTouched();
  }

  private showValidationError(): void {
    this.messageService.add({
      severity: 'error',
      summary: 'Ошибка валидации',
      detail: 'Пожалуйста, исправьте ошибки в форме'
    });
  }

  private submitForm(user: User): void {
    this.isLoading = true;
    const userRequest: UserRequest = {
      username: this.userForm.value.username,
      password: this.userForm.value.password || undefined
    };
    const rolesRequest: RoleRequest = {
      roles: this.userForm.value.roles || []
    };
    this.userForm.get('username')?.setErrors(null);
    this.userForm.get('password')?.setErrors(null);
    this.userService.updateUser(user.id, userRequest).subscribe({
      next: (updatedUser) => {
        this.updateUserRoles(updatedUser, rolesRequest);
      },
      error: (error) => {
        this.handleError('Ошибка обновления данных', 'Ошибка при обновлении данных пользователя', error);
        this.isLoading = false;
      }
    });
  }

  private updateUserRoles(user: User, rolesRequest: RoleRequest): void {
    this.userService.updateUserRoles(user.id, rolesRequest).subscribe({
      next: (updatedUserWithRoles) => {
        this.handleUserUpdateSuccess(updatedUserWithRoles);
      },
      error: (error) => {
        this.handleError('Ошибка обновления ролей', 'Ошибка при обновлении ролей пользователя', error);
        this.isLoading = false;
      }
    });
  }

  private handleUserUpdateSuccess(updatedUser: User): void {
    this.isLoading = false;
    this.changesOccurred.emit(true);
    this.showSuccessMessage('Пользователь обновлён', updatedUser);
    this.userSubmit.emit(updatedUser);
  }

  private showSuccessMessage(summary: string, user: User): void {
    this.messageService.add({
      severity: 'success',
      summary,
      detail: `Пользователь "${user.username}" успешно обновлён`
    });
  }

  private handleError(summary: string, detail: string, error: HttpErrorResponse): void {
    let errorMessage = detail;
    if (error.status === 400 && error.error && error.error.message) {
      errorMessage = error.error.message;
    }
    if ((error.status === 400 || error.status === 409) && error.error && error.error.details) {
      const details = error.error.details as ErrorDetail[];
      details.forEach(detail => {
        if (detail.field === 'username') {
          this.userForm.get('username')?.setErrors({ serverError: detail.message });
        } else if (detail.field === 'password') {
          this.userForm.get('password')?.setErrors({ serverError: detail.message });
        }
      });
      return;
    }
    this.messageService.add({ severity: 'error', summary, detail: errorMessage });
    console.error(`${summary}:`, error);
    this.isLoading = false;
  }

  protected readonly Role = Role;
}
