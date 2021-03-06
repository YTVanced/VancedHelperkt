package commands.`fun`

import commandhandler.CommandContext
import commands.base.BaseCommand
import config
import ext.hasQuotePerms
import ext.optional
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.requests.ErrorResponse
import org.koin.core.component.inject
import repository.gender.GenderRepositoryImpl
import type.CommandType.Fun

class Gender : BaseCommand(
    commandName = "gender",
    commandDescription = "Guess the gender",
    commandType = Fun,
    commandArguments = mapOf("The thing".optional()),
    commandAliases = listOf("sex")
) {

    private val repository by inject<GenderRepositoryImpl>()

    override fun execute(ctx: CommandContext) {
        super.execute(ctx)
        if (ctx.authorAsMember?.hasQuotePerms(guildId) == false) {
            ctx.message.replyMsg("You are not allowed to use this command")
            return
        }
        val args = ctx.args
        val event = ctx.event
        if (args.isNotEmpty()) {
            if (args[0].contains(contentIDRegex)) {
                ctx.guild.retrieveMemberById(contentIDRegex.find(args[0])!!.value).queue({
                    ctx.message.detectGender(it.user.name)
                }, ErrorHandler().handle(ErrorResponse.UNKNOWN_MEMBER) {
                    ctx.message.replyMsg("Provided member does not exist!")
                }.handle(ErrorResponse.UNKNOWN_USER) {
                    ctx.message.replyMsg("Provided user does not exist!")
                })
            } else {
                ctx.message.detectGender(args.joinToString(" "))
            }
        } else {
            ctx.message.detectGender(event.author.name)
        }

    }

    private fun Message.detectGender(name: String) {
        val filteredName = name.filter { it.isLetter() }
        val email = "${filteredName.replace(" ", ".")}@gmail.com"
        CoroutineScope(Dispatchers.IO).launch {
            val response = repository.get(
                token = config.genderToken,
                email = email
            )
            replyMsg(
                embedBuilder.apply {
                    setTitle("Gender Detector")
                    setDescription("$filteredName is ${response.gender}\nAccuracy: ${response.accuracy}%")
                    setFooter("Powered by gender-api.com")
                }.build()
            )
        }
    }

}