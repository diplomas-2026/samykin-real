import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { employeesApi } from '../api/services';
import { ErrorBanner } from '../components/ui/ErrorBanner';
import { Loader } from '../components/ui/Loader';
import { PageHeader } from '../components/ui/PageHeader';
import { UserAvatar } from '../components/ui/UserAvatar';
import { getApiErrorMessage } from '../utils/api';

export function EmployeesPage() {
  const [employees, setEmployees] = useState(null);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [departmentFilter, setDepartmentFilter] = useState('ALL');
  const [sortBy, setSortBy] = useState('name-asc');

  useEffect(() => {
    employeesApi.getAll()
      .then(setEmployees)
      .catch((requestError) => {
        setError(getApiErrorMessage(requestError, 'Не удалось загрузить сотрудников'));
      });
  }, []);

  const departments = useMemo(() => {
    if (!employees) {
      return [];
    }
    return [...new Set(employees.map((employee) => employee.department).filter(Boolean))].sort((left, right) => left.localeCompare(right, 'ru'));
  }, [employees]);

  if (!employees) {
    if (error) {
      return (
        <div className="stack">
          <PageHeader
            eyebrow="Сотрудники"
            title="Получатели денежных выплат"
            description="Список сотрудников, по которым бухгалтер ведет выплаты и контролирует статусы."
          />
          <ErrorBanner message={error} />
        </div>
      );
    }
    return <Loader text="Загрузка сотрудников..." />;
  }

  const normalizedSearch = search.trim().toLowerCase();
  const filteredEmployees = employees
    .filter((employee) => {
      if (departmentFilter !== 'ALL' && employee.department !== departmentFilter) {
        return false;
      }

      if (!normalizedSearch) {
        return true;
      }

      return [
        employee.fullName,
        employee.email,
        employee.department,
        employee.position,
        employee.employeeCode,
      ]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(normalizedSearch));
    })
    .sort((left, right) => {
      switch (sortBy) {
        case 'department-asc':
          return left.department.localeCompare(right.department, 'ru');
        case 'code-asc':
          return left.employeeCode.localeCompare(right.employeeCode, 'ru');
        case 'position-asc':
          return left.position.localeCompare(right.position, 'ru');
        case 'name-asc':
        default:
          return left.fullName.localeCompare(right.fullName, 'ru');
      }
    });

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Сотрудники"
        title="Получатели денежных выплат"
        description="Список сотрудников, по которым бухгалтер ведет выплаты и контролирует статусы."
      />
      <ErrorBanner message={error} />
      <div className="panel">
        <div className="filters-bar">
          <label className="filter-field filter-field--search">
            Поиск
            <input
              type="search"
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="ФИО, email, подразделение, должность или табельный номер"
            />
          </label>

          <label className="filter-field">
            Подразделение
            <select value={departmentFilter} onChange={(event) => setDepartmentFilter(event.target.value)}>
              <option value="ALL">Все подразделения</option>
              {departments.map((department) => (
                <option key={department} value={department}>{department}</option>
              ))}
            </select>
          </label>

          <label className="filter-field">
            Сортировка
            <select value={sortBy} onChange={(event) => setSortBy(event.target.value)}>
              <option value="name-asc">По ФИО А-Я</option>
              <option value="department-asc">По подразделению</option>
              <option value="position-asc">По должности</option>
              <option value="code-asc">По табельному номеру</option>
            </select>
          </label>
        </div>

        {filteredEmployees.length === 0 ? (
          <div className="empty-state">
            <strong>Сотрудники не найдены</strong>
            <p>Попробуйте изменить строку поиска, подразделение или выбранную сортировку.</p>
          </div>
        ) : null}

        {filteredEmployees.length > 0 ? (
          <table className="table">
            <thead>
              <tr>
                <th>Сотрудник</th>
                <th>Подразделение</th>
                <th>Должность</th>
                <th>Email</th>
                <th>Табельный номер</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {filteredEmployees.map((employee) => (
                <tr key={employee.id}>
                  <td>
                    <Link className="table-link table-user-cell" to={`/employees/${employee.id}`}>
                      <UserAvatar user={employee} size="xs" />
                      <span>{employee.fullName}</span>
                    </Link>
                  </td>
                  <td>{employee.department}</td>
                  <td>{employee.position}</td>
                  <td>{employee.email}</td>
                  <td><code>{employee.employeeCode}</code></td>
                  <td><Link to={`/employees/${employee.id}`}>Открыть</Link></td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : null}
      </div>
    </div>
  );
}
