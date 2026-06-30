import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Cross-field validator that ensures `endField` is strictly after `startField`.
 *
 * Apply this validator to a FormGroup, not to individual controls.
 * Sets a `dateRangeInvalid` error on the end-date control when the constraint
 * is violated; clears it otherwise.
 *
 * Usage:
 *   fb.group(
 *     { startDate: [''], endDate: [''] },
 *     { validators: dateRangeValidator('startDate', 'endDate') }
 *   )
 */
export function dateRangeValidator(
  startField: string,
  endField: string
): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const startControl = group.get(startField);
    const endControl = group.get(endField);

    if (!startControl || !endControl) {
      return null; // Controls not yet initialised
    }

    const startValue = startControl.value;
    const endValue = endControl.value;

    // Skip validation when either field is empty
    if (!startValue || !endValue) {
      if (endControl.hasError('dateRangeInvalid')) {
        removeError(endControl, 'dateRangeInvalid');
      }
      return null;
    }

    const start = new Date(startValue).getTime();
    const end = new Date(endValue).getTime();

    if (isNaN(start) || isNaN(end)) {
      return null; // Let other validators handle unparseable dates
    }

    if (end <= start) {
      const error = { dateRangeInvalid: { message: 'End date must be after start date' } };
      endControl.setErrors({ ...endControl.errors, ...error });
      return error;
    }

    // Dates are valid — remove only the dateRangeInvalid error
    if (endControl.hasError('dateRangeInvalid')) {
      removeError(endControl, 'dateRangeInvalid');
    }

    return null;
  };
}

/** Remove a single named error from a control without clearing unrelated errors. */
function removeError(control: AbstractControl, errorKey: string): void {
  const errors = { ...control.errors };
  delete errors[errorKey];
  control.setErrors(Object.keys(errors).length > 0 ? errors : null);
}
