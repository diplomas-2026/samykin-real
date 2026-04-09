import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { usersApi } from '../api/services';
import { ErrorBanner } from '../components/ui/ErrorBanner';
import { Loader } from '../components/ui/Loader';
import { PageHeader } from '../components/ui/PageHeader';
import { PasswordField } from '../components/ui/PasswordField';
import { getApiErrorMessage } from '../utils/api';

const emptyForm = {
  email: '',
  fullName: '',
  department: '',
  position: '',
  employeeCode: '',
  role: 'EMPLOYEE',
  active: true,
  password: '',
};

export function UserFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [form, setForm] = useState(emptyForm);
  const [fieldErrors, setFieldErrors] = useState({});
  const [submitError, setSubmitError] = useState('');
  const [loading, setLoading] = useState(Boolean(id));
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    usersApi.getById(id)
      .then((user) => {
        setForm({ ...user, password: '' });
        setSubmitError('');
      })
      .catch((requestError) => {
        setSubmitError(getApiErrorMessage(requestError, 'Не удалось загрузить данные пользователя'));
      })
      .finally(() => setLoading(false));
  }, [id]);

  const validateForm = () => {
    const nextErrors = {};

    if (!form.fullName.trim()) nextErrors.fullName = 'Укажите ФИО пользователя';
    if (!form.email.trim()) nextErrors.email = 'Укажите email';
    if (!form.department.trim()) nextErrors.department = 'Укажите подразделение';
    if (!form.position.trim()) nextErrors.position = 'Укажите должность';
    if (!form.employeeCode.trim()) nextErrors.employeeCode = 'Укажите табельный номер';
    if (!form.role.trim()) nextErrors.role = 'Выберите роль';
    if (!id && !form.password.trim()) nextErrors.password = 'Укажите пароль для нового пользователя';

    setFieldErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const updateField = (name, value) => {
    setForm((current) => ({ ...current, [name]: value }));
    setFieldErrors((current) => {
      if (!current[name]) {
        return current;
      }
      const nextErrors = { ...current };
      delete nextErrors[name];
      return nextErrors;
    });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!validateForm()) {
      setSubmitError('Заполните обязательные поля формы');
      return;
    }

    setSaving(true);
    setSubmitError('');
    try {
      if (id) {
        await usersApi.update(id, form);
      } else {
        await usersApi.create(form);
      }
      navigate('/users');
    } catch (requestError) {
      setSubmitError(getApiErrorMessage(requestError, id ? 'Не удалось сохранить изменения пользователя' : 'Не удалось создать пользователя'));
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <Loader text="Загружаем профиль пользователя..." />;
  }

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Администрирование"
        title={id ? 'Редактирование пользователя' : 'Создание пользователя'}
        description="Администратор назначает роль и управляет учетными записями."
      />
      <ErrorBanner message={submitError} />
      <form className="panel form-grid" onSubmit={handleSubmit}>
        <div className="full-width form-hint">Поля, отмеченные <span className="field-required">*</span>, обязательны для заполнения.</div>

        <label className="form-field"> 
          <span className="form-field__label">ФИО <span className="field-required">*</span></span>
          <input required value={form.fullName} disabled={saving} onChange={(event) => updateField('fullName', event.target.value)} aria-invalid={Boolean(fieldErrors.fullName)} />
          {fieldErrors.fullName ? <p className="field-error">{fieldErrors.fullName}</p> : null}
        </label>

        <label className="form-field">
          <span className="form-field__label">Email <span className="field-required">*</span></span>
          <input type="email" required value={form.email} disabled={saving} onChange={(event) => updateField('email', event.target.value)} aria-invalid={Boolean(fieldErrors.email)} />
          {fieldErrors.email ? <p className="field-error">{fieldErrors.email}</p> : null}
        </label>

        <label className="form-field">
          <span className="form-field__label">Подразделение <span className="field-required">*</span></span>
          <input required value={form.department} disabled={saving} onChange={(event) => updateField('department', event.target.value)} aria-invalid={Boolean(fieldErrors.department)} />
          {fieldErrors.department ? <p className="field-error">{fieldErrors.department}</p> : null}
        </label>

        <label className="form-field">
          <span className="form-field__label">Должность <span className="field-required">*</span></span>
          <input required value={form.position} disabled={saving} onChange={(event) => updateField('position', event.target.value)} aria-invalid={Boolean(fieldErrors.position)} />
          {fieldErrors.position ? <p className="field-error">{fieldErrors.position}</p> : null}
        </label>

        <label className="form-field">
          <span className="form-field__label">Табельный номер <span className="field-required">*</span></span>
          <input required value={form.employeeCode} disabled={saving} onChange={(event) => updateField('employeeCode', event.target.value)} aria-invalid={Boolean(fieldErrors.employeeCode)} />
          {fieldErrors.employeeCode ? <p className="field-error">{fieldErrors.employeeCode}</p> : null}
        </label>

        <label className="form-field">
          <span className="form-field__label">Роль <span className="field-required">*</span></span>
          <select required value={form.role} disabled={saving} onChange={(event) => updateField('role', event.target.value)} aria-invalid={Boolean(fieldErrors.role)}>
            <option value="ADMIN">Администратор</option>
            <option value="ACCOUNTANT">Бухгалтер</option>
            <option value="EMPLOYEE">Сотрудник</option>
          </select>
          {fieldErrors.role ? <p className="field-error">{fieldErrors.role}</p> : null}
        </label>

        <PasswordField
          label="Пароль"
          name="password"
          value={form.password}
          onChange={(event) => updateField('password', event.target.value)}
          placeholder={id ? 'Оставьте пустым, чтобы не менять' : 'Введите пароль'}
          required={!id}
          disabled={saving}
          error={fieldErrors.password}
          helperText={id ? 'При редактировании пароль можно не менять.' : 'Пароль обязателен для нового пользователя.'}
        />
        <label className="checkbox-row">
          <input type="checkbox" checked={form.active} disabled={saving} onChange={(event) => updateField('active', event.target.checked)} />
          Учетная запись активна
        </label>
        <button className="primary-button" type="submit" disabled={saving}>
          {saving ? (id ? 'Сохраняем...' : 'Создаем...') : (id ? 'Сохранить' : 'Создать')}
        </button>
      </form>
    </div>
  );
}
