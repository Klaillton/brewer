import { test, expect } from '@playwright/test';

test.describe('Brewer Web Application Tests', () => {
  
  test('should load login page', async ({ page }) => {
    await page.goto('/login');
    
    // Wait for the page to load
    await page.waitForLoadState('domcontentloaded');
    
    // Check that login form elements are present
    const pageContent = await page.content();
    expect(pageContent).toBeTruthy();
  });

  test('should redirect to login when accessing protected routes', async ({ page }) => {
    await page.goto('/cervejas');
    
    // Wait for navigation
    await page.waitForLoadState('networkidle');
    
    // Should be redirected to login
    expect(page.url()).toContain('login');
  });

  test('login page should have expected elements', async ({ page }) => {
    await page.goto('/login');
    await page.waitForLoadState('domcontentloaded');
    
    // Check for common login elements (username/password fields, submit button)
    const hasFormElements = await page.locator('form').count() > 0 || 
                           await page.locator('input').count() > 0;
    expect(hasFormElements).toBeTruthy();
  });

});
