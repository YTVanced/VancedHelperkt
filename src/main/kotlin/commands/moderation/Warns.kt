package commands.moderation

import com.mongodb.BasicDBObject
import commandhandler.CommandContext
import commands.base.BaseCommand
import database.warnsCollection
import ext.required
import ext.takeMax
import ext.useArguments
import ext.useCommandProperly
import org.litote.kmongo.findOne
import type.CommandType.Moderation

class Warns : BaseCommand(
    commandName = "warns",
    commandDescription = "Get warns for specified user",
    commandType = Moderation,
    commandArguments = mapOf("User ID | User Mention".required())
) {

    override fun execute(ctx: CommandContext) {
        super.execute(ctx)
        val args = ctx.args
        if (args.isNotEmpty()) {
            val user = args[0]
            val id = user.filter { it.isDigit() }
            if (id.isEmpty()) {
                ctx.message.useCommandProperly()
                return
            }
            val filter = BasicDBObject("userId", id).append("guildId", guildId)
            val warn = warnsCollection.findOne(filter)
            if (warn != null) {
                val reasons = warn.reasons
                if (reasons.isNotEmpty()) {
                    ctx.message.replyMsg(
                        embedBuilder.apply {
                            setTitle("Warns for ${warn.userName}")
                            for (i in reasons.indices) {
                                addField(
                                    "Warn ${i + 1}",
                                    reasons[i].takeMax(1024),
                                    false
                                )
                            }
                        }.build()
                    )
                } else {
                    ctx.message.replyMsg("User $user has no warns")
                }
            } else {
                ctx.message.replyMsg("User $user has no warns")
            }
        } else {
            ctx.message.useArguments(1)
        }
    }

}