package com.example.dididemo


//import com.nanorep.nanoengine.*
//import com.nanorep.nanoengine.model.NRAccount
//import com.nanorep.nanoengine.model.conversation.Conversation
//import com.nanorep.nanoengine.model.conversation.statement.StatementResponse
import android.app.Application
import android.os.Handler
import android.util.Log
import androidx.lifecycle.*
import com.nanorep.nanoengine.Entity
import com.nanorep.nanoengine.NRAction
import com.nanorep.nanoengine.PersonalInfoRequest
import com.nanorep.nanoengine.bot.BotAccount
import com.nanorep.nanoengine.bot.BotChat
import com.nanorep.nanoengine.bot.BotChatListener
import com.nanorep.nanoengine.model.conversation.statement.*
import com.nanorep.nanoengine.nonbot.EntitiesProvider
import com.nanorep.sdkcore.model.StatementScope
import com.nanorep.sdkcore.utils.Completion
import com.nanorep.sdkcore.utils.NRError
import com.nanorep.sdkcore.utils.TextTagHandler
import com.nanorep.sdkcore.utils.network.HttpRequest
import com.nanorep.sdkcore.utils.network.OnDataResponse
import com.nanorep.sdkcore.utils.network.Response
import java.util.*
import kotlin.collections.set


class BotChatViewModel(
    application: Application,
) : AndroidViewModel(application) {


    private lateinit var botChat: BotChat
    private val flag = false
    val responseLiveData: MutableLiveData<String> = MutableLiveData()

    private val botChatListener = object : BotChatListener {
        // A mandatory method to be implemented:
        override fun conversationIdUpdated(conversationId: String) { // Conversation initialized and the engine is ready
            Log.d("engine_response", "conversationIdUpdated, conversationId = $conversationId")
            startChatWithBot()
        }

        // The statement response callback
        override fun onResponse(response: StatementResponse) { // Here we listen to the statements responses
            handleStatementResponse(response)
            Log.d("engine_response", TextTagHandler.getSpannedHtml(response.text).toString())
        }

        // Error event listener
        override fun onError(error: NRError) {
            Log.e("engine_response", "onError: $error")
        }
    }

    private val ddGreetings: String
        get() {
            return if (flag) {
                "dd-search-hello-greeting"
            } else {
                "dd-hello-greeting"
            }
        }
    private val ddSubjects: String
        get() {
            return if (flag) {
                "dd-search-subjects"
            } else {
                "dd-subjects"
            }
        }

    fun initBotChat() {
        initNanoRep()
    }

    private fun initNanoRep() {
        //get Cookies.

        //setHeaders.

        //set ConversationContext params.
        val conversationContext = HashMap<String, String>()
        val appType = "DISCOUNT"
        val Os = "Android"
        val AppVersion = "61.0.0"
        val prefixAppVersion = "Android"
        val pernum = ""

        conversationContext["APPTYPE"] = appType
        conversationContext["UNIQUEID"] = pernum
        conversationContext["OS"] = Os
        conversationContext["AppVersion"] = prefixAppVersion + AppVersion


// ***** Initialization *****

// 1. Creates the Bot account - an account that enables conversation with the Bot.
        val account = BotAccount(
            "fb34e6f9-b80e-49de-b4b5-618e9bc02f60",
            "DiscountBankCloud",
            "DIDIAppTest",
            "",
            conversationContext
        )

// 2. The Engine creation:
        botChat = BotChat(account)

// 3. Applies the headers to all of the REST requests
//        if (DiscountApplication.IS_TESTING) {
//            account.httpHeaders.putAll(httpHeaders)
//        }

// 4. Sets the timeout for the HttpRequests(the default is 3000 milliseconds)
        HttpRequest.defaultTimeout = 10000

// 5. Sets entites to be provided to the knowladge base (via the KB providers)
        account.entities = arrayOf("Beneficiary", "MerchantNameSearch")



// 7. Engine listener - listens to events:

        botChat.setEntitiesProvider(object : EntitiesProvider {
            override fun provide(personalInfoRequest: PersonalInfoRequest, callback: PersonalInfoRequest.Callback) {
                callback.onInfoReady("for an immediate loan", NRAction(personalInfoRequest.id).apply {
                    Log.d("personalInfo", "${personalInfoRequest.id} ${personalInfoRequest.userInfo}")
                    if (userInfo != null) {
                        personalInfoRequest.userInfo?.let{ info -> this.userInfo?.putAll(info)}
                        //this.userInfo.putAll(personalInfoRequest.userInfo)
                    }

                }) // the action is kind of parallel retrieved info - The customer should set it according to its needs
                // this action will be added to the actions list on the StatementResponse
            }



            override fun provide(info: ArrayList<String>, callback: Completion<ArrayList<Entity>>) {

            }

        })
        botChat.setBotChatListener(botChatListener)
        botChat.initConversation()


    }
    // 6. Conversation initialization - creates the conversation at the server:


    fun sendStatementToEngine(statement: String) {/*, postback: Boolean) {*/
        //nanorep?.sendStatement(statement, postback, conversationId, null, OnBotEngineResponse())

        Log.d("didi_chat", "sendStatementToEngine(), statement = $statement")

        botChat.postStatement(PostbackRequest().postback(statement), object : OnStatementResponse {
            override fun onResponse(response: StatementResponse) {
                response.error?.run { onError(this) }
            }

            override fun onResponse(response: Response<StatementResponse>) {
                response.data.takeUnless { it == null }?.run { onResponse(this) }
                    ?: onError(response.error ?: NRError(NRError.GeneralError, NRError.EmptyResponse))
            }

            override fun onError(error: NRError) {
                Log.d("error", "$error")
            }
        })
       //botChat.postStatement(PostbackRequest().postback(statement), botChatListener)

//        botChat.postStatement(
//            StatementFactory.create(
//                OutgoingStatement(
//                    statement,
//                    StatementScope.NanoBotScope,
//                    InputMethod.ManuallyTyped.toInputSource()
//                )
//            )
//        )
        //Saving the last statement that nano rep will be handling

    }

    fun startChatWithBot() {
        startWithGreetingText()
    }

    private fun startWithGreetingText() {
        Log.d("didi_chat", "startWithGreetingText()")

        sendStatementToEngine(ddGreetings)
        Handler().postDelayed({ sendDDSubjects() }, 500)
    }

    private fun sendDDSubjects() {
        Log.d("didi_chat", "sendDDSubjects()")
        sendStatementToEngine(ddSubjects)
    }

    private fun handleStatementResponse(response: StatementResponse?) {
        handleBotAnswer(response)
    }

    private fun handleBotAnswer(response: StatementResponse?) {
        Log.d("didi_chat", "handleBotAnswer(), response = ${response.toString()}")



        //Bot answers array - in some case both will have two and more responses.
        var botAnswers: ArrayList<String?> = ArrayList()

        //The response text to show in the chat.
        var botAnswer: String = response?.multiAnswers?.takeUnless { it.isEmpty() }?.joinToString() ?: response?.text ?: ">> Response not available <<"
            /*if (response?.statement != null) {
                response.multiAnswers.joinToString()

            } else {
                response?.text  //didi . statement - text invoked from prev statement
            }*/

        Log.i("BE-Response", botAnswer)
        responseLiveData.value = botAnswer
        botAnswers.add(botAnswer)
    }

}