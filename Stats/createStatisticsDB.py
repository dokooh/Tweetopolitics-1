###
#
# KTH - Royal Institute of Technology
# Read Tweets from Mongo Database and extract statistics about users and tags
# Filippia Zikou
#
###

#Retreive congiguration info
import ConfigParser

#Communication with mongo
from pymongo import MongoClient 
from bson.code import Code
from pymongo import Connection

# Reading the config file
config = ConfigParser.RawConfigParser()
config.read('read.cfg')

#Reading database settings
client = MongoClient()
db = client[config.get('mongodb', 'db')]
collection = config.get('mongodb', 'collection')
statistics_collection = config.get('mongodb', 'stats_collection')

#Reading parties
partyNames = config.get('parties', 'names').split(',')

#Reading Party Users
partyUsers = {}
for party in partyNames:
	partyUsers[party] = config.get('partyUsers', party).split(',')
	
#Reading party hashtags
partyTags = {}
for party in partyNames:
	partyTags[party] = config.get('partyTags', party).split(',')
	
#Specify the maximum length to send through socket
MAX_LEN = 150000

###
#
#  A function that creates the statists database
#  The database is created with each username as the unique id
#  Initialized with a map reduce function to extract:
#  1. The number of posts of each user
#  2. The number of mentions of each user
#  3. The hashatag of each user and the number of occurance
#  4. The users that user is communicating the most - "friends"
#
###
def map_reduce():
	print "Step 1: Apply map reduce for posts, mentions and hashtags..."
	print "Please wait... this may take a while..."
	
	
	#Number of times that user is mentioned
	mentions_map = Code("""function(){ 
		userMentions = this.entities.user_mentions; 
		for (var i=0 ; i < userMentions.length ; i++) { 
			if (userMentions[i].screen_name.length > 0 ) {
				emit(userMentions[i].screen_name, {mentions: 1, posts: 0, hashtags: [], friends: []});
			}
		}
	}""")
		
	#Number of posts per user
	posts_map = Code("""
	function(){ 
	emit(this.user.screen_name,  {mentions: 0, posts: 1, hashtags: [], friends: []});
	}
	""")
	
	#Hashtags and occurence times per user
	hashtags_map = Code("""function(){ 
	    var total_hashtag_texts = [];
		userHashtags = this.entities.hashtags; 
		for (var i=0 ; i < userHashtags.length ; i++) {
			if (userHashtags[i].text.length > 0)
				total_hashtag_texts.push(userHashtags[i].text);
		}
		emit(this.user.screen_name, {mentions: 0, posts: 0,  hashtags: total_hashtag_texts, friends: []});
	}""")
	
	#People that each user interacts with
	friends_map = Code("""function(){ 
	    var total_user_friends =[];
		userFriends = this.entities.user_mentions; 
		for (var i=0 ; i < userFriends.length ; i++) { 
			if (userFriends[i].screen_name.length > 0 ) 
				total_user_friends.push(userFriends[i].screen_name);
		}
		emit(this.user.screen_name, {mentions: 0, posts: 0, hashtags: [], friends:  total_user_friends});
	}""")
	
	
	r = Code("""function(key, values) {
	   var result = {mentions: 0, posts: 0, hashtags: [], friends: []};
	   values.forEach(function(value) {
	      result.mentions += (value.mentions!= null) ? value.mentions :0;
	      result.posts += (value.posts!= null) ? value.posts :0;
	      result.hashtags.push.apply(result.hashtags,value.hashtags);
	      result.friends.push.apply(result.friends,value.friends);
	   });
	   return result;
	}""")
	
	#map reduce functions and store to the new collection
	db[collection].map_reduce(mentions_map ,r, {'reduce':statistics_collection})
	print 'mentions done'
	db[collection].map_reduce(posts_map,r,  {'reduce':statistics_collection})
	print 'posts done'
	db[collection].map_reduce(hashtags_map,r,  {'reduce':statistics_collection})
	print 'tags done'
	db[collection].map_reduce(friends_map,r,  {'reduce':statistics_collection})
	print 'friends done'

		
	
	
def tag_politicians():
	db[statistics_collection].update({}, {'$set' :{'party':'none'}}, multi=True)
	for party in partyNames:
		db[statistics_collection].update({'_id': {'$in': partyUsers[party]}}, {'$set' :{'party': party}}, multi=True)

		
def find_party(hashtags, friends):
	matchParties=[]
	for party in partyNames:
		if len(set(hashtags)&set(partyTags[party])) > 0 or   len(set(friends)&set(partyUsers[party])):
			matchParties.append(party)
	if len(matchParties) == 1:
		return matchParties[0]
	else:
		return 'none'
		
def tag_users():
	for doc in db[statistics_collection].find({'party':'none'}):
		party = find_party(doc['value']['hashtags'], doc['value']['friends'])
		if party != 'none':
			db[statistics_collection].update({'_id': doc['_id']}, {'$set' :{'party': party}})
	
	
if __name__ == '__main__':
	map_reduce()
	tag_politicians()
	tag_users()

