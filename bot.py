import asyncio
import codecs
import datetime
import time
import humanfriendly
import io
import itertools
import json
import logging
import math
import os
import pprint
import random
import re
import shutil
import string
import subprocess
import sys
import threading
import traceback
import typing
import urllib.parse
import urllib.request

from io import StringIO
from typing import Any
from typing import Iterable
from typing import Tuple

import aiofiles
import aiohttp
import discord
import requests

from async_timeout import timeout
from discord import AsyncWebhookAdapter
from discord import RequestsWebhookAdapter
from discord import Webhook
from discord.ext import commands
from discord.ext import tasks

url_rx = re.compile(r'https?://(?:www\.)?.+')
upsince = datetime.datetime.now()

logchannel = None


class CommandErrorHandler(commands.Cog):

	def __init__(self, bot):
		self.bot = bot

	@commands.Cog.listener()
	async def on_command_error(self, ctx, error):
		if hasattr(ctx.command, 'on_error'):
			return
		ignored = (commands.CommandNotFound, commands.UserInputError)
		error = getattr(error, 'original', error)
		if isinstance(error, ignored):
			return
		elif isinstance(error, commands.DisabledCommand):
			return await ctx.send(f'{ctx.command} has been disabled.')
		elif isinstance(error, commands.NoPrivateMessage):
			try:
				return await ctx.author.send(f'{ctx.command} can not be used in Private Messages.')
			except:
				pass
		elif isinstance(error, commands.BadArgument):
			if ctx.command.qualified_name == 'tag list':
				return await ctx.send('I could not find that member. Please try again.')
		print('Ignoring exception in command {}:'.format(ctx.command), file=sys.stderr)
		traceback.print_exception(
			type(error), error, error.__traceback__, file=sys.stderr)


class HelpCommand(commands.Cog):

	@commands.command(name="help")
	async def help_(self, ctx):
		embed = discord.Embed(
			title="My commands!", description="Made by ThatOneCalculator and Pixer!", color=0x0000FF)
		embed.add_field(name="`!!ping`", value="Pings the bot")
		embed.add_field(name="`!!literallynobot`", value="Directs you to ThatOneCalculator's public bot LiterallyNoBot")
		embed.add_field(name="`!!dumpy (width)`", value="Makes a dumpy gif from whatever image you post (or whatever is the last image in chat), with width being an optional number between 2 and 30, the default is 9.")
		await ctx.send(embed=embed)

	@commands.command()
	async def literallynobot(self, ctx):
		await ctx.send("https://top.gg/bot/646156214237003777")


class TheStuff(commands.Cog):

	def __init__(self, bot):
		self.bot = bot
		self.update_status.start()

	@commands.command(aliases=["twerk", "amogus"])
	async def dumpy(self, ctx, number: typing.Union[discord.Member, int, str] = 9):
		if type(number) != int: number = 9
		number = str(number)
		async with ctx.typing():
			try:
				async for message in ctx.channel.history(limit=20):
					if len(message.attachments) > 0:
						await message.attachments[0].save("attach.png")
			except:
				return await ctx.send("Couldn't find image!")
		async with ctx.typing():
			subprocess.run("java", "sus", str(number), "attach.png", capture_output=True, text=True)
			filename = "dumpy.gif"
			await ctx.send(file=discord.File(filename, filename=filename))

	@commands.command(name="ping")
	async def ping(self, ctx):
		ping = await ctx.send(f":ping_pong: Pong! Bot latency is {str(round((bot.latency * 1000),2))} milliseconds.")
		beforeping = datetime.datetime.now()
		await ping.edit(content="Pinging!")
		afterping = datetime.datetime.now()
		pingdiff = afterping - beforeping
		pingdiffms = pingdiff.microseconds / 1000
		uptime = afterping - upsince
		await ping.edit(content=f"🏓 Pong! Bot latency is {str(round((bot.latency * 1000),2))} milliseconds.\n☎️ API latency is {str(round((pingdiffms),2))} milliseconds.\n:coffee: I have been up for {humanfriendly.format_timespan(uptime)}.")

	@tasks.loop(minutes=10)
	async def update_status(self):
		await self.bot.wait_until_ready()
		await bot.change_presence(activity=discord.Activity(
							type=discord.ActivityType.watching,
							name=f"!!dumpy on {len(self.bot.guilds)}"
							))


intents = discord.Intents.default()
bot = commands.AutoShardedBot(command_prefix=commands.when_mentioned_or(
	"!!"), intents=intents, chunk_guilds_at_startup=False)
bot.remove_command("help")
bot.add_cog(HelpCommand(bot))
bot.add_cog(TheStuff(bot))
bot.add_cog(CommandErrorHandler(bot))


@bot.event
async def on_ready():
	print("Ready")


def read_token():
	with open("token.txt", "r") as f:
		lines = f.readlines()
		return lines[0].strip()

bot.run(read_token())