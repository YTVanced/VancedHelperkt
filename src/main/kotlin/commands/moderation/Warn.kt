package commands.moderation

import commandhandler.CommandContext
import commands.base.BaseCommand
import ext.*
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.requests.ErrorResponse
import type.CommandType.Moderation

class Warn : BaseCommand(
    commandName = "warn",
    commandDescription = "Warn a user",
    commandType = Moderation,
    commandArguments = mapOf("User ID | User Mention".required(), "Reason".optional())
) {

    override fun execute(ctx: CommandContext) {
        super.execute(ctx)
        val args = ctx.args
        if (args.isNotEmpty()) {
            val user = args[0]
            val reason = if (args.size > 1) args.apply { remove(user) }.joinToString(" ") else "no reason provided"
            val id = user.filter { it.isDigit() }
            if (id.isEmpty()) {
                ctx.message.useCommandProperly()
                return
            }
            ctx.guild.retrieveMemberById(id).queue({ member ->
                if (!ctx.authorAsMember?.canInteract(member)!!) {
                    ctx.message.replyMsg("You can't warn this member!")
                    return@queue
                }
                member.warn(ctx.author, guildId, reason, ctx.channel)
                ctx.message.replyMsg("Successfully warned ${member.user.asMention}")
            }, ErrorHandler().handle(ErrorResponse.UNKNOWN_USER) {
                ctx.message.replyMsg("Provided user does not exist!")
            }.handle(ErrorResponse.UNKNOWN_MEMBER) {
                ctx.message.replyMsg("Provided member does not exist!")
            })

        } else {
            ctx.message.useArguments(1)
        }
    }

}