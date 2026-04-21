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