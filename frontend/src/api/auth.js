import request from './request'

export function login(username, password) {
  return request({
    url: '/auth/login',
    method: 'post',
    data: { username, password }
  })
}

export function register(username, password) {
  return request({
    url: '/auth/register',
    method: 'post',
    data: { username, password }
  })
}

export function getCurrentUser() {
  return request({
    url: '/auth/me',
    method: 'get'
  })
}
