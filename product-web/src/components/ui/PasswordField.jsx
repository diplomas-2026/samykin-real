import { useId, useState } from 'react';

export function PasswordField({
  label,
  value,
  onChange,
  placeholder,
  name,
}) {
  const [visible, setVisible] = useState(false);
  const inputId = useId();

  return (
    <label htmlFor={inputId}>
      {label}
      <div className="password-field">
        <input
          id={inputId}
          name={name}
          type={visible ? 'text' : 'password'}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
        />
        <button
          type="button"
          className="password-toggle"
          onClick={() => setVisible((current) => !current)}
          aria-label={visible ? 'Скрыть пароль' : 'Показать пароль'}
          title={visible ? 'Скрыть пароль' : 'Показать пароль'}
        >
          {visible ? 'Скрыть' : 'Показать'}
        </button>
      </div>
    </label>
  );
}
