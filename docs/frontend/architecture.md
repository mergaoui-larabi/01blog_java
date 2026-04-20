# architecture

## folder structure

This is the tree of the frontend spa:

```tree
frontend/
│
├── src/
│   ├── app/
│   │   ├── core/              # services (API calls)
│   │   ├── shared/            # reusable components
│   │   ├── features/
│   │   │   ├── auth/
│   │   │   ├── posts/
│   │   │   ├── profile/
│   │   │   ├── notifications/
│   │   │   └── admin/
│   │   │
│   │   ├── layouts/
│   │   └── app-routing.module.ts
│   │
│   ├── assets/
│   └── environments/
│       ├── environment.ts
│       └── environment.prod.ts
│
├── angular.json
└── package.json
```

## Angular CLI 

first u install the cli :

```sh
npm install -g @angular/cli
```

using angular cli we can generate the spa:

```sh
ng new frontend
cd frontend
ng generate module features/posts
ng generate component features/posts/post-list
```
