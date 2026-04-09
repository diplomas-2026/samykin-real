import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  fullyParallel: false,
  use: {
    baseURL: 'http://localhost:5173',
    screenshot: 'off',
  },
  reporter: [['list']],
});
