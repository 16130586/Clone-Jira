import { of as rxjsOf } from 'rxjs';
import { ajax } from 'rxjs/ajax';
import { mergeMap, delay, map, switchMap, catchError, mapTo } from 'rxjs/operators';
import { ofType } from 'redux-observable';
import { Auth as AuthEventTypes } from '../constants/index'
import { BACKEND_API } from '../config/api'
import { tokenValid, tokenInValid, madeRequestFail, loginSuccess, loginFailed } from '../actions/global'

export const validateToken = action$ =>
  action$.pipe(
    ofType(AuthEventTypes.VALIDATE_TOKEN),
    mergeMap(action => {
      const fullyUrl = BACKEND_API.BASE_URL.concat(BACKEND_API.ACTIONS.VALIDATE_TOKEN)
      const requestSettings = {
        url: fullyUrl,
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: {
          token: action.payload
        }
      }
      return ajax(requestSettings)
        .pipe(
          mergeMap(ajaxResponse => rxjsOf({ status: ajaxResponse.status, response: ajaxResponse.response })),
          catchError(ajaxOnError => rxjsOf({ status: ajaxOnError.status, response: ajaxOnError.message })))
    }),
    map(ajax => {
      if (ajax.status == 0)
        return madeRequestFail(ajax.response)
      if (ajax.response == null)
        return madeRequestFail('No response from server!')
      else if (ajax.status > 0 && ajax.response.status < 400) {
        return tokenValid(ajax.response.data)
      }
      else
        return tokenInValid()
    })
  );


export const login = action$ =>
  action$.pipe(
    ofType(AuthEventTypes.LOGIN),
    mergeMap(action => {
      const fullyUrl = BACKEND_API.BASE_URL.concat(BACKEND_API.ACTIONS.LOGIN)
      const requestSettings = {
        url: fullyUrl,
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: {
          userName: action.payload.userName,
          password: action.payload.password
        }
      }
      return ajax(requestSettings)
        .pipe(
          mergeMap(ajaxResponse => rxjsOf({ status: ajaxResponse.status, response: ajaxResponse.response })),
          catchError(ajaxOnError => rxjsOf({ status: ajaxOnError.status, response: ajaxOnError.message })))
    }),
    map(ajax => {
      // xhr failed on cros, 404 , 500 - xhr not by our server
      if (ajax.status == 0)
        return madeRequestFail(ajax.response)
      // xhr request was success but server didn't return anything
      if (ajax.response == null)
        return madeRequestFail('No response from server!')
      // xhr request was sucess and 
      else if (ajax.status > 0 && ajax.response.status < 400) {
        return loginSuccess(ajax.response.data)
      }
      else
        return loginFailed()
    })
  )