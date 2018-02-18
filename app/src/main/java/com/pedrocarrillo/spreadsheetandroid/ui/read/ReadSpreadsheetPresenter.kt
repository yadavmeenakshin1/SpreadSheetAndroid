package com.pedrocarrillo.spreadsheetandroid.ui.read

import com.google.api.services.sheets.v4.Sheets
import com.pedrocarrillo.spreadsheetandroid.data.model.Person
import com.pedrocarrillo.spreadsheetandroid.ui.read.ReadSpreadsheetContract.Presenter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

/**
 * @author Pedro Carrillo
 */

class ReadSpreadsheetPresenter(val view: ReadSpreadsheetContract.View,
                               val authenticationManager: AuthenticationManager,
                               val sheetsApi : SheetsAPIManager) : Presenter {

    lateinit var readSpreadsheetDisposable : Disposable

    override fun startAuthentication() {
        view.launchAuthentication(authenticationManager.googleSignInClient)
    }

    override fun init() {
        startAuthentication()
    }

    override fun dispose() {
        readSpreadsheetDisposable.dispose()
    }

    override fun loginSuccessful() {
        view.showName(authenticationManager.getLastSignedAccount()?.displayName!!)
        authenticationManager.setUpGoogleAccountCredential()
        startReadingSpreadsheet(spreadsheetId, range, sheetsApi.sheetsAPI)
    }

    override fun loginFailed() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun startReadingSpreadsheet(spreadsheetId : String, range : String, sheet : Sheets) {
        readSpreadsheetDisposable=
                Observable
                .fromCallable{
                    val response = sheet.spreadsheets().values()
                            .get(spreadsheetId, range)
                            .execute()
                    response.getValues() }
                .flatMapIterable { it -> it }
                .map { Person(it[0].toString(), it[4].toString()) }
                .toList()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { view.showError(it.localizedMessage) }
                .subscribe(Consumer {
                    view.showPeople(it)
                })
    }

    companion object {
        val spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms"
        val range = "Class Data!A2:E"
    }
}