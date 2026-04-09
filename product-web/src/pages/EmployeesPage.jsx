import { useEffect, useState } from 'react';
import { employeesApi } from '../api/services';
import { Loader } from '../components/ui/Loader';
import { PageHeader } from '../components/ui/PageHeader';
import { UserAvatar } from '../components/ui/UserAvatar';

export function EmployeesPage() {
  const [employees, setEmployees] = useState(null);

  useEffect(() => {
    employeesApi.getAll().then(setEmployees);
  }, []);

  if (!employees) return <Loader text="Загрузка сотрудников..." />;

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Сотрудники"
        title="Получатели денежных выплат"
        description="Справочник сотрудников, для которых оформляются денежные выплаты."
      />
      <div className="card-grid">
        {employees.map((employee) => (
          <div key={employee.id} className="employee-card">
            <div className="employee-card__header">
              <UserAvatar user={employee} />
              <div className="stack-xs">
                <strong>{employee.fullName}</strong>
                <span className="card-meta">{employee.position}</span>
              </div>
            </div>
            <span>{employee.department}</span>
            <span>{employee.email}</span>
            <code>{employee.employeeCode}</code>
          </div>
        ))}
      </div>
    </div>
  );
}
