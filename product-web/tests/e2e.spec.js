import fs from 'node:fs';
import path from 'node:path';
import { test, expect } from '@playwright/test';

const currentDir = path.dirname(new URL(import.meta.url).pathname);
const usersFile = path.resolve(currentDir, '../../product-api/users.txt');
const screenshotsDir = path.resolve(currentDir, '../artifacts/screenshots');

function parseUsers() {
  const raw = fs.readFileSync(usersFile, 'utf8').trim().split('\n');
  return raw.map((line) => Object.fromEntries(
    line.split(';').map((part) => part.trim().split('=')),
  ));
}

async function takeShot(page, fileName) {
  await page.screenshot({
    path: path.join(screenshotsDir, fileName),
    fullPage: true,
  });
}

async function login(page, user) {
  await page.goto('/login');
  await expect(page.getByRole('heading', { name: 'Добро пожаловать в Samykin Pay' })).toBeVisible();
  await page.getByLabel('Email').fill(user.email);
  await page.locator('input[name="password"]').fill(user.password);
  await page.getByRole('button', { name: 'Войти' }).click();
}

async function logout(page) {
  const logoutButton = page.getByRole('button', { name: 'Выйти' });
  if (await logoutButton.isVisible()) {
    await logoutButton.click();
  }
}

test.beforeAll(() => {
  fs.rmSync(screenshotsDir, { recursive: true, force: true });
  fs.mkdirSync(screenshotsDir, { recursive: true });
});

test('публичные страницы и сценарии администратора', async ({ page }) => {
  const admin = parseUsers().find((user) => user.role === 'ADMIN');

  await page.goto('/');
  await expect(page.getByRole('heading', { name: 'Распределение и выдача денежных выплат без ручной рутины' })).toBeVisible();
  await takeShot(page, '01-landing-page.png');

  await page.goto('/login');
  await expect(page.getByRole('heading', { name: 'Добро пожаловать в Samykin Pay' })).toBeVisible();
  await takeShot(page, '02-login-page.png');

  await login(page, admin);
  await expect(page).toHaveURL(/\/dashboard$/);

  await expect(page.getByText('Финансовая панель бухгалтера')).toBeVisible();
  await takeShot(page, '03-dashboard-admin-editable-ai-settings.png');

  await page.goto('/users');
  await expect(page.getByText('Пользователи системы')).toBeVisible();
  await takeShot(page, '04-users-list.png');

  await page.goto('/users/new');
  await expect(page.getByText('Создание пользователя')).toBeVisible();
  await takeShot(page, '05-user-create-form.png');

  await page.locator('form').evaluate((form) => form.setAttribute('novalidate', 'true'));
  await page.getByRole('button', { name: 'Создать' }).click();
  await expect(page.getByText('Заполните обязательные поля формы')).toBeVisible();
  await takeShot(page, '06-user-create-validation-state.png');

  await page.goto('/users');
  await page.getByRole('link', { name: 'Редактировать' }).first().click();
  await expect(page.getByText('Редактирование пользователя')).toBeVisible();
  await takeShot(page, '07-user-edit-form.png');

  await page.goto('/profile');
  await expect(page.getByText('Информация о текущем пользователе системы.')).toBeVisible();
  await takeShot(page, '08-admin-profile-page.png');

  await logout(page);
});

test('сценарии бухгалтера и состояния разделов выплат', async ({ page }) => {
  const accountant = parseUsers().find((user) => user.role === 'ACCOUNTANT');

  await login(page, accountant);
  await expect(page).toHaveURL(/\/dashboard$/);

  await expect(page.getByText('Финансовая панель бухгалтера')).toBeVisible();
  await takeShot(page, '09-dashboard-accountant-summary-ai-settings.png');

  await page.goto('/employees');
  await expect(page.getByText('Получатели денежных выплат')).toBeVisible();
  await takeShot(page, '10-employees-list.png');

  await page.getByPlaceholder('ФИО, email, подразделение, должность или табельный номер').fill('несуществующий сотрудник');
  await expect(page.getByText('Сотрудники не найдены')).toBeVisible();
  await takeShot(page, '11-employees-empty-state.png');

  await page.goto('/employees');
  await page.getByRole('link', { name: /Алексей Сидоров/i }).click();
  await expect(page.getByRole('heading', { name: 'Алексей Сидоров' })).toBeVisible();
  await takeShot(page, '12-employee-details-page.png');

  await page.goto('/payouts');
  await expect(page.getByText('Журнал денежных выплат')).toBeVisible();
  await takeShot(page, '13-payouts-list.png');

  await page.getByPlaceholder('Код, сотрудник или тип выплаты').fill('нет такой выплаты');
  await expect(page.getByText('Ничего не найдено')).toBeVisible();
  await takeShot(page, '14-payouts-empty-state.png');

  await page.goto('/payouts/new');
  await expect(page.getByText('Оформление денежной выплаты')).toBeVisible();
  await takeShot(page, '15-payout-create-form.png');

  await page.goto('/payouts');
  await page.getByRole('row', { name: /PAY-00003/ }).getByRole('link', { name: 'Открыть' }).click();
  await expect(page.getByText('Карточка выплаты')).toBeVisible();
  await takeShot(page, '16-payout-details-created.png');

  await page.goto('/payouts');
  await page.getByRole('row', { name: /PAY-00002/ }).getByRole('link', { name: 'Открыть' }).click();
  await expect(page.getByText('Карточка выплаты')).toBeVisible();
  await takeShot(page, '17-payout-details-prepared.png');

  await page.goto('/payouts');
  await page.getByRole('row', { name: /PAY-00001/ }).getByRole('link', { name: 'Открыть' }).click();
  await expect(page.getByText('Карточка выплаты')).toBeVisible();
  await takeShot(page, '18-payout-details-paid.png');

  await page.goto('/profile');
  await expect(page.getByText('Информация о текущем пользователе системы.')).toBeVisible();
  await takeShot(page, '19-accountant-profile-page.png');

  await logout(page);
});

test('сценарии сотрудника и ai-помощник', async ({ page }) => {
  const employee = parseUsers().find((user) => user.email === 'ivanov@samykin.local');

  await login(page, employee);
  await expect(page).toHaveURL(/\/my-payouts$/);

  await expect(page.getByText('Личный журнал выплат')).toBeVisible();
  await takeShot(page, '20-employee-my-payouts.png');

  await page.goto('/assistant');
  await expect(page.getByText('Чат по вашим выплатам')).toBeVisible();
  await takeShot(page, '21-assistant-initial-state.png');

  await page.getByPlaceholder('Например: какие выплаты у меня еще не выданы?').fill('привет');
  await page.getByRole('button', { name: 'Отправить вопрос' }).click();
  await expect(page.getByText('Я могу помочь с вашими выплатами')).toBeVisible();
  await takeShot(page, '22-assistant-chat-history-state.png');

  await page.goto('/profile');
  await expect(page.getByText('Фотография профиля')).toBeVisible();
  await takeShot(page, '23-employee-profile-page.png');
});
