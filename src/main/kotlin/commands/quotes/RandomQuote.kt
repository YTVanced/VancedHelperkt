package commands.quotes

import commandhandler.CommandContext
import commands.base.BaseCommand
import database.collections.Quote
import database.quotesCollection
import ext.getQuote
import org.litote.kmongo.eq
import type.CommandType.Quotes

class RandomQuote : BaseCommand(
    commandName = "randomquote",
    commandDescription = "Get a random quote",
    commandType = Quotes,
    commandAliases = listOf("rq", "random")
) {

    override fun execute(ctx: CommandContext) {
        super.execute(ctx)
        val args = ctx.args
        if (args.isNotEmpty()) {
            val userId = args[0].filter { it.isDigit() }
            if (userId.matches(contentIDRegex)) {
                val quote = quotesCollection.find(Quote::authorID eq userId)
                try {
                    ctx.message.replyMsg(getQuote(quote.toList().random()))
                } catch (e: NoSuchElementException) {
                    ctx.message.replyMsg("There are no quotes in this server from the provided! Try adding some")
                }
            }
        } else {
            try {
                ctx.message.replyMsg(getQuote(quotesCollection.find().toList().random()))
            } catch (e: NoSuchElementException) {
                ctx.message.replyMsg("There are no quotes in this server! Try adding some")
            }
        }


    }

}