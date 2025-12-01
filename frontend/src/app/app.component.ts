import { Component } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  template: `
    <header class="navbar">
      <div class="container">
        <h1><a routerLink="/">Brewer</a></h1>
        <nav>
          <a routerLink="/cervejas">Cervejas</a>
        </nav>
      </div>
    </header>
    <main class="container">
      <router-outlet></router-outlet>
    </main>
  `,
  styles: [`
    .navbar {
      background-color: #343a40;
      color: white;
      padding: 15px 0;
    }
    .navbar .container {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .navbar h1 {
      margin: 0;
      font-size: 24px;
    }
    .navbar h1 a {
      color: white;
      text-decoration: none;
    }
    .navbar nav a {
      color: white;
      text-decoration: none;
      margin-left: 20px;
    }
    .navbar nav a:hover {
      text-decoration: underline;
    }
  `]
})
export class AppComponent {
  title = 'brewer-frontend';
}
