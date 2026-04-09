import { useId, useState } from 'react';

export function PasswordField({
  label,
  value,
  onChange,
  placeholder,
  name,
  required = false,
  disabled = false,
  error = '',
  helperText = '',
}) {
  const [visible, setVisible] = useState(false);
  const inputId = useId();
  const errorId = `${inputId}-error`;
  const helperId = `${inputId}-helper`;
  const describedBy = [error ? errorId : '', !error && helperText ? helperId : ''].filter(Boolean).join(' ') || undefined;

  return (
    <label htmlFor={inputId} className="form-field">
      <span className="form-field__label">
        {label}
        {required ? <span className="field-required"> *</span> : null}
      </span>
      <div className="password-field">
        <input
          id={inputId}
          name={name}
          type={visible ? 'text' : 'password'}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          required={required}
          disabled={disabled}
          aria-invalid={Boolean(error)}
          aria-describedby={describedBy}
        />
        <button
          type="button"
          className="password-toggle"
          onClick={() => setVisible((current) => !current)}
          disabled={disabled}
          aria-label={visible ? 'Скрыть пароль' : 'Показать пароль'}
          title={visible ? 'Скрыть пароль' : 'Показать пароль'}
        >
          {visible ? 'Скрыть' : 'Показать'}
        </button>
      </div>
      {error ? <p id={errorId} className="field-error">{error}</p> : null}
      {!error && helperText ? <p id={helperId} className="field-hint">{helperText}</p> : null}
    </label>
  );
}
