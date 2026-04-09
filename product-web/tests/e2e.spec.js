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

async function login(page, user) {
  await page.goto('/login');
  await page.getByLabel('Email').fill(user.email);
  await page.getByLabel('Пароль').fill(user.password);
  await page.getByRole('button', { name: 'Войти' }).click();
}

test.beforeAll(() => {
  fs.mkdirSync(screenshotsDir, { recursive: true });
});

test('администратор видит страницу пользователей', async ({ page }) => {
  const admin = parseUsers().find((user) => user.role === 'ADMIN');
  await page.goto('/');
  await page.screenshot({ path: path.join(screenshotsDir, '01-landing.png'), fullPage: true });
  await login(page, admin);
  await page.goto('/users');
  await expect(page.getByText('Пользователи системы')).toBeVisible();
  await page.screenshot({ path: path.join(screenshotsDir, '02-login-admin.png'), fullPage: true });
  await page.screenshot({ path: path.join(screenshotsDir, '03-users.png'), fullPage: true });
});

test('бухгалтер видит дашборд и выплаты', async ({ page }) => {
  const accountant = parseUsers().find((user) => user.role === 'ACCOUNTANT');
  await login(page, accountant);
  await expect(page.getByText('Финансовая панель бухгалтера')).toBeVisible();
  await page.screenshot({ path: path.join(screenshotsDir, '04-dashboard.png'), fullPage: true });
  await page.goto('/payouts');
  await expect(page.getByText('Журнал денежных выплат')).toBeVisible();
  await page.screenshot({ path: path.join(screenshotsDir, '05-payouts.png'), fullPage: true });
  await page.goto('/payouts/new');
  await expect(page.getByText('Оформление денежной выплаты')).toBeVisible();
  await page.screenshot({ path: path.join(screenshotsDir, '06-payout-form.png'), fullPage: true });
});

test('сотрудник видит только свои выплаты', async ({ page }) => {
  const employee = parseUsers().find((user) => user.role === 'EMPLOYEE');
  await login(page, employee);
  await page.goto('/my-payouts');
  await expect(page.getByText('Личный журнал выплат')).toBeVisible();
  await page.screenshot({ path: path.join(screenshotsDir, '07-my-payouts.png'), fullPage: true });
});
