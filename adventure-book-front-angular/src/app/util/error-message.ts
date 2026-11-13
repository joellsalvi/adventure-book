import {HttpErrorResponse} from '@angular/common/http';
import {ApiError} from '../model/book.model';

export function toErrorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    const apiMessage = (error.error as Partial<ApiError> | null)?.message;
    if (apiMessage) {
      return apiMessage;
    }
    if (error.status === 0 || error.status >= 500) {
      return `The Adventure Book API is unavailable (HTTP ${error.status}). Make sure it is running on http://localhost:8080.`;
    }
    return `Request failed (HTTP ${error.status}).`;
  }
  if (error instanceof Error) {
    return error.message;
  }
  return 'Unexpected error.';
}
