package commands.moderation

import commandhandler.CommandContext
import commands.base.BaseCommand
import ext.required
import ext.useArguments
import type.CommandType.Moderation
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

class Purge : BaseCommand(
    commandName = "purge",
    commandDescription = "Purge a number of messages between 2 and 100 in the channel",
    commandType = Moderation,
    commandAliases = listOf("prune", "clear"),
    commandArguments = mapOf("amount".required())
) {

    override fun execute(ctx: CommandContext) {
        super.execute(ctx)
        val args = ctx.args
        if (args.isNotEmpty()) {
            val amount = args[0].toIntOrNull()
            if (amount == null || amount > 100 || amount < 2) {
                ctx.message.replyMsg("Provide a valid amount!")
                return
            }
            ctx.channel.history.retrievePast(amount).queue { messagesList ->
                val messages = messagesList.filter { !it.timeCreated.isBefore(OffsetDateTime.now().minus(2, ChronoUnit.WEEKS)) }.take(100)
                if (messages.isEmpty()) {
                    ctx.message.replyMsg("Messages not found!")
                    return@queue
                }
                ctx.channel.deleteMessages(messages).queue {
                    ctx.message.replyMsg("Succesfully deleted $amount messages")
                }
            }
        } else {
            ctx.message.useArguments(1)
        }

    }

}