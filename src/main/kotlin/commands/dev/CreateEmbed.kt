package commands.dev

import com.google.gson.Gson
import commandhandler.CommandContext
import commands.base.BaseCommand
import ext.required
import ext.useCommandProperly
import type.CommandType.Dev
import java.awt.Color

class CreateEmbed : BaseCommand(
    commandName = "createembed",
    commandDescription = "Create an embed from a provided json",
    commandType = Dev,
    commandArguments = mapOf("json".required()),
    commandAliases = listOf("embed"),
    devOnly = true
) {

    override fun execute(ctx: CommandContext) {
        super.execute(ctx)
        val args = ctx.args
        if (args.isNotEmpty()) {
            val json = args.joinToString(" ")
            ctx.message.replyMsg(
                embedBuilder.apply {
                    try {
                        with(Gson().fromJson(json, JsonEmbed::class.java)) {
                            setTitle(this?.title)
                            setDescription(this?.description)
                            if (this?.fields != null) {
                                this.fields.forEach {
                                    addField(
                                        it.title,
                                        it.content,
                                        false
                                    )
                                }
                            }
                            setImage(this?.image)
                            setThumbnail(this?.thumbnail)
                            setFooter(this?.footer)

                            if (this?.color != null) {
                                setColor(Color.decode(this.color))
                            }
                        }
                    } catch (e: Exception) {
                        ctx.message.replyMsg("Could not create an embed")
                        return
                    }
                }.build()
            )
        } else {
            ctx.message.useCommandProperly()
        }
    }

}