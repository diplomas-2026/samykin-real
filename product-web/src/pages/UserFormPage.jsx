import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { usersApi } from '../api/services';
import { PageHeader } from '../components/ui/PageHeader';

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

  useEffect(() => {
    if (!id) return;
    usersApi.getById(id).then((user) => setForm({ ...user, password: '' }));
  }, [id]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (id) {
      await usersApi.update(id, form);
    } else {
      await usersApi.create(form);
    }
    navigate('/users');
  };

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Администрирование"
        title={id ? 'Редактирование пользователя' : 'Создание пользователя'}
        description="Администратор назначает роль и управляет учетными записями."
      />
      <form className="panel form-grid" onSubmit={handleSubmit}>
        <label>ФИО<input value={form.fullName} onChange={(event) => setForm((current) => ({ ...current, fullName: event.target.value }))} /></label>
        <label>Email<input type="email" value={form.email} onChange={(event) => setForm((current) => ({ ...current, email: event.target.value }))} /></label>
        <label>Подразделение<input value={form.department} onChange={(event) => setForm((current) => ({ ...current, department: event.target.value }))} /></label>
        <label>Должность<input value={form.position} onChange={(event) => setForm((current) => ({ ...current, position: event.target.value }))} /></label>
        <label>Табельный номер<input value={form.employeeCode} onChange={(event) => setForm((current) => ({ ...current, employeeCode: event.target.value }))} /></label>
        <label>Роль
          <select value={form.role} onChange={(event) => setForm((current) => ({ ...current, role: event.target.value }))}>
            <option value="ADMIN">Администратор</option>
            <option value="ACCOUNTANT">Бухгалтер</option>
            <option value="EMPLOYEE">Сотрудник</option>
          </select>
        </label>
        <label>Пароль<input type="password" value={form.password} onChange={(event) => setForm((current) => ({ ...current, password: event.target.value }))} /></label>
        <label className="checkbox-row">
          <input type="checkbox" checked={form.active} onChange={(event) => setForm((current) => ({ ...current, active: event.target.checked }))} />
          Учетная запись активна
        </label>
        <button className="primary-button" type="submit">{id ? 'Сохранить' : 'Создать'}</button>
      </form>
    </div>
  );
}
