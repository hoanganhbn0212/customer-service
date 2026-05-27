const TOKEN_KEY = "accessToken";
const TOKEN_TYPE_KEY = "tokenType";
const USER_NAME_KEY = "rememberUserName";
const ROLE_KEY = "userRole";

export function getAccessToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function getTokenType() {
  return localStorage.getItem(TOKEN_TYPE_KEY) || "Bearer";
}

export function getUserRole() {
  return localStorage.getItem(ROLE_KEY);
}

export function isAdmin() {
  return getUserRole() === "ADMIN";
}

export function isDevelop() {
  return getUserRole() === "DEVELOP";
}

/** Admin or Develop — may edit theme / page configuration */
export function canEditPages() {
  return isAdmin() || isDevelop();
}

/** Full user & package management */
export function canManageUsers() {
  return isAdmin();
}

export function setSession(loginResponse) {
  localStorage.setItem(TOKEN_KEY, loginResponse.accessToken);
  localStorage.setItem(TOKEN_TYPE_KEY, loginResponse.tokenType || "Bearer");
  const role = loginResponse.role;
  if (role) {
    localStorage.setItem(ROLE_KEY, typeof role === "string" ? role : String(role));
  }
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(TOKEN_TYPE_KEY);
  localStorage.removeItem(ROLE_KEY);
}

export function isAuthenticated() {
  return Boolean(getAccessToken());
}

export function getRememberedUserName() {
  return localStorage.getItem(USER_NAME_KEY);
}

export function setRememberedUserName(userName) {
  localStorage.setItem(USER_NAME_KEY, userName);
}

export function clearRememberedUserName() {
  localStorage.removeItem(USER_NAME_KEY);
}
