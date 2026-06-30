import { TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';
import { AppComponent } from './app.component';
import { AuthService } from './shared/services/auth.service';

const userOnly = { username: 'carol@example.com', roles: ['USER'], firstName: 'Carol', lastName: 'Reader' };
const manager   = { username: 'bob@citylibrary.org', roles: ['MANAGER'], firstName: 'Bob', lastName: 'Staff' };
const admin     = { username: 'alice@citylibrary.org', roles: ['ADMIN'], firstName: 'Alice', lastName: 'Admin' };

function buildMock(user: object | null) {
  return { currentUser$: of(user), logout: jasmine.createSpy('logout') };
}

async function setup(user: object | null) {
  const mock = buildMock(user);
  await TestBed.configureTestingModule({
    imports: [AppComponent, RouterTestingModule],
    providers: [{ provide: AuthService, useValue: mock }],
  }).compileComponents();
  const fixture = TestBed.createComponent(AppComponent);
  fixture.detectChanges();
  return { fixture, app: fixture.componentInstance, mock, el: fixture.nativeElement as HTMLElement };
}

describe('AppComponent — nav state', () => {

  afterEach(() => TestBed.resetTestingModule());

  describe('when not logged in', () => {
    it('isLoggedIn is false', async () => {
      const { app } = await setup(null);
      expect(app.isLoggedIn).toBeFalse();
    });

    it('isAdmin is false', async () => {
      const { app } = await setup(null);
      expect(app.isAdmin).toBeFalse();
    });

    it('isStaff is false', async () => {
      const { app } = await setup(null);
      expect(app.isStaff).toBeFalse();
    });

    it('shows Login link', async () => {
      const { el } = await setup(null);
      expect(el.querySelector('a[href="/login"]') || el.textContent).toContain('Login');
    });

    it('shows Register link', async () => {
      const { el } = await setup(null);
      expect(el.textContent).toContain('Register');
    });

    it('hides all nav groups', async () => {
      const { el } = await setup(null);
      expect(el.querySelectorAll('.nav-group').length).toBe(0);
    });

    it('does not show Log Out', async () => {
      const { el } = await setup(null);
      expect(el.textContent).not.toContain('Log Out');
    });
  });

  describe('when logged in as USER', () => {
    it('isLoggedIn is true', async () => {
      const { app } = await setup(userOnly);
      expect(app.isLoggedIn).toBeTrue();
    });

    it('isAdmin is false', async () => {
      const { app } = await setup(userOnly);
      expect(app.isAdmin).toBeFalse();
    });

    it('isStaff is false', async () => {
      const { app } = await setup(userOnly);
      expect(app.isStaff).toBeFalse();
    });

    it('shows Log Out', async () => {
      const { el } = await setup(userOnly);
      expect(el.textContent).toContain('Log Out');
    });

    it('does not show Login', async () => {
      const { el } = await setup(userOnly);
      expect(el.textContent).not.toContain('Login');
    });

    it('shows standard nav groups (Books, My Account, Events)', async () => {
      const { el } = await setup(userOnly);
      expect(el.textContent).toContain('Books');
      expect(el.textContent).toContain('My Account');
      expect(el.textContent).toContain('Events');
    });

    it('hides Staff nav group', async () => {
      const { el } = await setup(userOnly);
      const navGroups = Array.from(el.querySelectorAll('.nav-group'));
      const hasStaff = navGroups.some(g => g.textContent?.includes('Staff'));
      expect(hasStaff).toBeFalse();
    });

    it('hides Admin nav group', async () => {
      const { el } = await setup(userOnly);
      const navGroups = Array.from(el.querySelectorAll('.nav-group'));
      const hasAdmin = navGroups.some(g => g.textContent?.includes('Admin'));
      expect(hasAdmin).toBeFalse();
    });
  });

  describe('when logged in as MANAGER (staff)', () => {
    it('isStaff is true', async () => {
      const { app } = await setup(manager);
      expect(app.isStaff).toBeTrue();
    });

    it('isAdmin is false', async () => {
      const { app } = await setup(manager);
      expect(app.isAdmin).toBeFalse();
    });

    it('shows Staff nav group', async () => {
      const { el } = await setup(manager);
      const navGroups = Array.from(el.querySelectorAll('.nav-group'));
      const hasStaff = navGroups.some(g => g.textContent?.includes('Staff'));
      expect(hasStaff).toBeTrue();
    });

    it('hides Admin nav group', async () => {
      const { el } = await setup(manager);
      const navGroups = Array.from(el.querySelectorAll('.nav-group'));
      const hasAdmin = navGroups.some(g => g.textContent?.includes('Admin'));
      expect(hasAdmin).toBeFalse();
    });

    it('shows Log Out', async () => {
      const { el } = await setup(manager);
      expect(el.textContent).toContain('Log Out');
    });
  });

  describe('when logged in as ADMIN', () => {
    it('isStaff is true', async () => {
      const { app } = await setup(admin);
      expect(app.isStaff).toBeTrue();
    });

    it('isAdmin is true', async () => {
      const { app } = await setup(admin);
      expect(app.isAdmin).toBeTrue();
    });

    it('shows Staff nav group', async () => {
      const { el } = await setup(admin);
      const navGroups = Array.from(el.querySelectorAll('.nav-group'));
      const hasStaff = navGroups.some(g => g.textContent?.includes('Staff'));
      expect(hasStaff).toBeTrue();
    });

    it('shows Admin nav group', async () => {
      const { el } = await setup(admin);
      const navGroups = Array.from(el.querySelectorAll('.nav-group'));
      const hasAdmin = navGroups.some(g => g.textContent?.includes('Admin'));
      expect(hasAdmin).toBeTrue();
    });

    it('shows Log Out', async () => {
      const { el } = await setup(admin);
      expect(el.textContent).toContain('Log Out');
    });

    it('shows user initials in avatar', async () => {
      const { el } = await setup(admin);
      expect(el.querySelector('.user-avatar')?.textContent?.trim()).toBe('AA');
    });
  });

  describe('logout()', () => {
    it('calls authService.logout()', async () => {
      const { app, mock } = await setup(userOnly);
      app.logout();
      expect(mock.logout).toHaveBeenCalled();
    });
  });
});
