###### 公共内容接口
1. /api/public/bank-info
2. /api/public/products
3. /api/public/rates
4. /api/public/announcements

###### 受保护接口
1. /api/v1/accounts/**
2. /api/v1/transactions/**
3. /api/v1/cards/**
4. /api/v1/loans/**
5. /api/v1/profile/**


##### 已实现接口
###### Auth
1. POST /api/v1/auth/login
2. POST /api/v1/auth/register
3. POST /api/v1/auth/logout
4. POST /api/v1/auth/send-code

###### Account
1. POST  /api/v1/accounts - CreateAccount 
2. GET   /api/v1/accounts - GetAccounts
2. GET   /api/v1/accounts/{accountId} - GetAccountById
3. PATCH /api/v1/accounts/{accountId}/freeze - FreezeAccount
4. PATCH /api/v1/accounts/{accountId}/unfreeze - UnFreezeAccount
5. PATCH /api/v1/accounts/{accountId}/close - CloseAccount
6. PATCH //api/v1/accounts/{accountId}/unclose - UncloseAccount

###### Transaction
1. POST  /api/v1/transactions/deposit - Deposit
2. POST  /api/v1/transactions/withdraw - Withdraw
3. POST  /api/v1/transactions/transfer - Transfer
4. GET  /api/v1/transactions/{accountId} - See account transcript

###### User-Profile
1. GET  /api/v1/profile - See user profile
2. PUT  /api/v1/profile/update - Modify user profile
3. POST /api/v1/profile/email/request      申请修改 email → 发验证码
4. PUT  /api/v1/profile/email/confirm      确认验证码 → 更新 email + 换 token

###### Admin
1. GET    /api/v1/admin/users                -    查看所有用户
2. GET    /api/v1/admin/users/{id}           -    查看单个用户详情
3. GET    /api/v1/admin/users/{id}/profile   -    查看用户 Profile
4. PATCH  /api/v1/admin/users/{id}/freeze    -    冻结用户
5. PATCH  /api/v1/admin/users/{id}/unfreeze  -    解冻用户
6. PATCH  /api/v1/admin/users/{id}/role      -    分配角色




## Role & Permission Matrix

| Permission | CUSTOMER | TELLER | MANAGER | ADMIN |
|---|:---:|:---:|:---:|:---:|
| **ACCOUNT** | | | | |
| ACCOUNT:READ_OWN | ✅ | ✅ | ✅ | ✅ |
| ACCOUNT:READ_ALL | | ✅ | ✅ | ✅ |
| ACCOUNT:CREATE | ✅ | ✅ | ✅ | ✅ |
| ACCOUNT:UPDATE | | | | ✅ |
| ACCOUNT:FREEZE | | | ✅ | ✅ |
| ACCOUNT:UNFREEZE | | | | ✅ |
| ACCOUNT:CLOSE | | | | ✅ |
| **TRANSACTION** | | | | |
| TRANSACTION:CREATE | ✅ | ✅ | ✅ | ✅ |
| TRANSACTION:READ_OWN | ✅ | ✅ | ✅ | ✅ |
| TRANSACTION:READ_ALL | | ✅ | ✅ | ✅ |
| TRANSACTION:REVERSE | | | | ✅ |
| **AUDIT_LOG** | | | | |
| AUDIT_LOG:READ | | | ✅ | ✅ |
| AUDIT_LOG:EXPORT | | | | ✅ |
| **USER** | | | | |
| USER:READ_OWN | | | | ✅ |
| USER:READ_ALL | | | | ✅ |
| USER:CREATE | | | | ✅ |
| USER:UPDATE | | | | ✅ |
| USER:DELETE | | | | ✅ |
| USER:ASSIGN_ROLE | | | | ✅ |
| USER:FREEZE | | | | ✅ |
| **USER_PROFILE** | | | | |
| USER_PROFILE:READ_OWN | | | | ✅ |
| USER_PROFILE:UPDATE | | | | ✅ |