package com.leap12.databuddy.commands.http;

import com.leap12.common.HttpRequest;
import com.leap12.common.HttpResponse;
import com.leap12.common.Log;
import com.leap12.common.StrUtl;
import com.leap12.databuddy.BaseConnectionDelegate;
import com.leap12.databuddy.Commands.CmdResponse;
import com.leap12.databuddy.Commands.CmdResponse.CmdResponseMutable;

public class GithubHttpCmd extends HttpCmd {

	@Override
	public float isCommand( HttpRequest in ) {
		try {
			Log.d( "USER-AGENT: '%s'", in.getUserAgent() );
			return StrUtl.startsWith( in.getUserAgent(), "GitHub-Hookshot" ) ? 1f : 0;
		} catch ( Exception e ) {
			return 0f;
		}
	}

	@Override
	public CmdResponse<HttpResponse> executeCommand( BaseConnectionDelegate connection, HttpRequest input ) {
		try {
			HttpResponse response = new HttpResponse();
			connection.writeMsg( response.toString() );
			return new CmdResponseMutable<HttpResponse>( HttpResponse.class, response );
		} catch ( Exception e ) {
			return new CmdResponseMutable<HttpResponse>( HttpResponse.class, e );
		}
	}


	// POST / HTTP/1.1\r\n
	// Host: proxy.aarontharris.com:25566\r\n
	// Accept: */*\r\n
	// User-Agent: GitHub-Hookshot/9db916b\r\n
	// X-GitHub-Event: ping\r\n
	// X-GitHub-Delivery: ed5ff500-63fe-11e5-9fe0-59b73454498f\r\n
	// content-type: application/json\r\n
	// Content-Length: 6387\r\n
	// \r\n
	// {"zen":"Non-blocking is better than blocking.","hook_id":5943007,"hook":{"url":"https://api.github.com/repos/aarontharris/DataBuddy/hooks/5943007","test_url":"https://api.github.com/repos/aarontharris/DataBuddy/hooks/5943007/test","ping_url":"https://api.github.com/repos/aarontharris/DataBuddy/hooks/5943007/pings","id":5943007,"name":"web","active":true,"events":["pull_request","push"],"config":{"url":"http://proxy.aarontharris.com:25566","content_type":"json","insecure_ssl":"0","secret":""},"last_response":{"code":null,"status":"unused","message":null},"updated_at":"2015-09-26T03:30:26Z","created_at":"2015-09-26T03:30:26Z"},"repository":{"id":41659001,"name":"DataBuddy","full_name":"aarontharris/DataBuddy","owner":{"login":"aarontharris","id":4163081,"avatar_url":"https://avatars.githubusercontent.com/u/4163081?v=3","gravatar_id":"","url":"https://api.github.com/users/aarontharris","html_url":"https://github.com/aarontharris","followers_url":"https://api.github.com/users/aarontharris/followers","following_url":"https://api.github.com/users/aarontharris/following{/other_user}","gists_url":"https://api.github.com/users/aarontharris/gists{/gist_id}","starred_url":"https://api.github.com/users/aarontharris/starred{/owner}{/repo}","subscriptions_url":"https://api.github.com/users/aarontharris/subscriptions","organizations_url":"https://api.github.com/users/aarontharris/orgs","repos_url":"https://api.github.com/users/aarontharris/repos","events_url":"https://api.github.com/users/aarontharris/events{/privacy}","received_events_url":"https://api.github.com/users/aarontharris/received_events","type":"User","site_admin":false},"private":false,"html_url":"https://github.com/aarontharris/DataBuddy","description":"Portable datastore, can be deployed locally or remotely for game data storage.","fork":false,"url":"https://api.github.com/repos/aarontharris/DataBuddy","forks_url":"https://api.github.com/repos/aarontharris/DataBuddy/forks","keys_url":"https://api.github.com/repos/aarontharris/DataBuddy/keys{/key_id}","collaborators_url":"https://api.github.com/repos/aarontharris/DataBuddy/collaborators{/collaborator}","teams_url":"https://api.github.com/repos/aarontharris/DataBuddy/teams","hooks_url":"https://api.github.com/repos/aarontharris/DataBuddy/hooks","issue_events_url":"https://api.github.com/repos/aarontharris/DataBuddy/issues/events{/number}","events_url":"https://api.github.com/repos/aarontharris/DataBuddy/events","assignees_url":"https://api.github.com/repos/aarontharris/DataBuddy/assignees{/user}","branches_url":"https://api.github.com/repos/aarontharris/DataBuddy/branches{/branch}","tags_url":"https://api.github.com/repos/aarontharris/DataBuddy/tags","blobs_url":"https://api.github.com/repos/aarontharris/DataBuddy/git/blobs{/sha}","git_tags_url":"https://api.github.com/repos/aarontharris/DataBuddy/git/tags{/sha}","git_refs_url":"https://api.github.com/repos/aarontharris/DataBuddy/git/refs{/sha}","trees_url":"https://api.github.com/repos/aarontharris/DataBuddy/git/trees{/sha}","statuses_url":"https://api.github.com/repos/aarontharris/DataBuddy/statuses/{sha}","languages_url":"https://api.github.com/repos/aarontharris/DataBuddy/languages","stargazers_url":"https://api.github.com/repos/aarontharris/DataBuddy/stargazers","contributors_url":"https://api.github.com/repos/aarontharris/DataBuddy/contributors","subscribers_url":"https://api.github.com/repos/aarontharris/DataBuddy/subscribers","subscription_url":"https://api.github.com/repos/aarontharris/DataBuddy/subscription","commits_url":"https://api.github.com/repos/aarontharris/DataBuddy/commits{/sha}","git_commits_url":"https://api.github.com/repos/aarontharris/DataBuddy/git/commits{/sha}","comments_url":"https://api.github.com/repos/aarontharris/DataBuddy/comments{/number}","issue_comment_url":"https://api.github.com/repos/aarontharris/DataBuddy/issues/comments{/number}","contents_url":"https://api.github.com/repos/aarontharris/DataBuddy/contents/{+path}","compare_url":"https://api.github.com/repos/aarontharris/DataBuddy/compare/{base}...{head}","merges_url":"https://api.github.com/repos/aarontharris/DataBuddy/merges","archive_url":"https://api.github.com/repos/aarontharris/DataBuddy/{archive_format}{/ref}","downloads_url":"https://api.github.com/repos/aarontharris/DataBuddy/downloads","issues_url":"https://api.github.com/repos/aarontharris/DataBuddy/issues{/number}","pulls_url":"https://api.github.com/repos/aarontharris/DataBuddy/pulls{/number}","milestones_url":"https://api.github.com/repos/aarontharris/DataBuddy/milestones{/number}","notifications_url":"https://api.github.com/repos/aarontharris/DataBuddy/notifications{?since,all,participating}","labels_url":"https://api.github.com/repos/aarontharris/DataBuddy/labels{/name}","releases_url":"https://api.github.com/repos/aarontharris/DataBuddy/releases{/id}","created_at":"2015-08-31T06:13:47Z","updated_at":"2015-08-31T06:25:15Z","pushed_at":"2015-09-25T09:11:29Z","git_url":"git://github.com/aarontharris/DataBuddy.git","ssh_url":"git@github.com:aarontharris/DataBuddy.git","clone_url":"https://github.com/aarontharris/DataBuddy.git","svn_url":"https://github.com/aarontharris/DataBuddy","homepage":null,"size":7308,"stargazers_count":0,"watchers_count":0,"language":"Java","has_issues":true,"has_downloads":true,"has_wiki":true,"has_pages":false,"forks_count":0,"mirror_url":null,"open_issues_count":0,"forks":0,"open_issues":0,"watchers":0,"default_branch":"master"},"sender":{"login":"aarontharris","id":4163081,"avatar_url":"https://avatars.githubusercontent.com/u/4163081?v=3","gravatar_id":"","url":"https://api.github.com/users/aarontharris","html_url":"https://github.com/aarontharris","followers_url":"https://api.github.com/users/aarontharris/followers","following_url":"https://api.github.com/users/aarontharris/following{/other_user}","gists_url":"https://api.github.com/users/aarontharris/gists{/gist_id}","starred_url":"https://api.github.com/users/aarontharris/starred{/owner}{/repo}","subscriptions_url":"https://api.github.com/users/aarontharris/subscriptions","organizations_url":"https://api.github.com/users/aarontharris/orgs","repos_url":"https://api.github.com/users/aarontharris/repos","events_url":"https://api.github.com/users/aarontharris/events{/privacy}","received_events_url":"https://api.github.com/users/aarontharris/received_events","type":"User","site_admin":false}}

}
