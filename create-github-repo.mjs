import { Octokit } from '@octokit/rest';

let connectionSettings;

async function getAccessToken() {
  if (connectionSettings && connectionSettings.settings.expires_at && new Date(connectionSettings.settings.expires_at).getTime() > Date.now()) {
    return connectionSettings.settings.access_token;
  }
  
  const hostname = process.env.REPLIT_CONNECTORS_HOSTNAME;
  const xReplitToken = process.env.REPL_IDENTITY 
    ? 'repl ' + process.env.REPL_IDENTITY 
    : process.env.WEB_REPL_RENEWAL 
    ? 'depl ' + process.env.WEB_REPL_RENEWAL 
    : null;

  if (!xReplitToken) {
    throw new Error('X_REPLIT_TOKEN not found for repl/depl');
  }

  connectionSettings = await fetch(
    'https://' + hostname + '/api/v2/connection?include_secrets=true&connector_names=github',
    {
      headers: {
        'Accept': 'application/json',
        'X_REPLIT_TOKEN': xReplitToken
      }
    }
  ).then(res => res.json()).then(data => data.items?.[0]);

  const accessToken = connectionSettings?.settings?.access_token || connectionSettings.settings?.oauth?.credentials?.access_token;

  if (!connectionSettings || !accessToken) {
    throw new Error('GitHub not connected');
  }
  return accessToken;
}

async function getGitHubClient() {
  const accessToken = await getAccessToken();
  return new Octokit({ auth: accessToken });
}

async function createRepository() {
  try {
    const octokit = await getGitHubClient();
    
    // Get authenticated user
    const { data: user } = await octokit.rest.users.getAuthenticated();
    console.log(`✓ Authenticated as: ${user.login}`);
    
    // Create repository
    console.log('\nCreating GitHub repository...');
    const { data: repo } = await octokit.rest.repos.createForAuthenticatedUser({
      name: 'battery-monitor-android',
      description: 'Native Android app for real-time battery monitoring with voltage, current, power data, and resizable widget',
      private: false,
      auto_init: false
    });
    
    console.log(`✓ Repository created: ${repo.html_url}`);
    console.log(`✓ Clone URL: ${repo.clone_url}`);
    console.log(`✓ SSH URL: ${repo.ssh_url}`);
    
    // Return repository info
    return {
      url: repo.html_url,
      cloneUrl: repo.clone_url,
      sshUrl: repo.ssh_url,
      owner: user.login,
      name: repo.name
    };
  } catch (error) {
    if (error.status === 422 && error.message.includes('already exists')) {
      console.error('\n⚠️  Repository "battery-monitor-android" already exists in your account.');
      console.error('Please either:');
      console.error('  1. Delete the existing repository on GitHub');
      console.error('  2. Or modify the repository name in this script');
      process.exit(1);
    } else {
      console.error('\n❌ Error creating repository:', error.message);
      throw error;
    }
  }
}

createRepository()
  .then(repo => {
    console.log('\n' + '='.repeat(80));
    console.log('SUCCESS! Repository created successfully.');
    console.log('='.repeat(80));
    console.log(`\nRepository URL: ${repo.url}`);
    console.log(`Owner: ${repo.owner}`);
    console.log(`Name: ${repo.name}`);
    console.log('\nNext steps will initialize git and push your code...');
  })
  .catch(error => {
    console.error('\n❌ Failed to create repository:', error);
    process.exit(1);
  });
