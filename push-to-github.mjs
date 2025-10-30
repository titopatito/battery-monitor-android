import { Octokit } from '@octokit/rest';
import { execSync } from 'child_process';
import { writeFileSync } from 'fs';

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

function exec(command, options = {}) {
  try {
    return execSync(command, { encoding: 'utf8', stdio: 'pipe', ...options });
  } catch (error) {
    if (!options.ignoreError) {
      throw error;
    }
    return '';
  }
}

async function main() {
  console.log('════════════════════════════════════════════════════════════════════════════');
  console.log('  Battery Monitor - GitHub Repository Setup');
  console.log('════════════════════════════════════════════════════════════════════════════');
  console.log('');

  try {
    // Step 1: Get GitHub client and user info
    console.log('Step 1: Authenticating with GitHub...');
    const octokit = await getGitHubClient();
    const { data: user } = await octokit.rest.users.getAuthenticated();
    console.log(`✓ Authenticated as: ${user.login}`);
    console.log('');

    // Step 2: Create repository
    console.log('Step 2: Creating GitHub repository...');
    let repo;
    try {
      const { data } = await octokit.rest.repos.createForAuthenticatedUser({
        name: 'battery-monitor-android',
        description: 'Native Android app for real-time battery monitoring with voltage, current, power data, and resizable widget',
        private: false,
        auto_init: false
      });
      repo = data;
      console.log(`✓ Repository created: ${repo.html_url}`);
    } catch (error) {
      if (error.status === 422) {
        console.log('⚠️  Repository already exists, fetching existing repository...');
        const { data } = await octokit.rest.repos.get({
          owner: user.login,
          repo: 'battery-monitor-android'
        });
        repo = data;
        console.log(`✓ Using existing repository: ${repo.html_url}`);
      } else {
        throw error;
      }
    }
    console.log('');

    // Step 3: Initialize Git
    console.log('Step 3: Initializing Git repository...');
    const gitExists = exec('[ -d .git ] && echo "exists" || echo "not exists"', { ignoreError: true }).trim();
    
    if (gitExists !== 'exists') {
      exec('git init');
      console.log('✓ Git repository initialized');
    } else {
      console.log('✓ Git repository already initialized');
    }

    // Configure git
    exec('git config user.name "Replit User" || true', { ignoreError: true });
    exec('git config user.email "user@replit.com" || true', { ignoreError: true });
    console.log('✓ Git configured');
    console.log('');

    // Step 4: Add files
    console.log('Step 4: Adding files to Git...');
    exec('git add .');
    console.log('✓ Files staged for commit');
    console.log('');

    // Step 5: Commit
    console.log('Step 5: Creating initial commit...');
    const commitMessage = `Initial commit: Battery Monitor Android App

Features:
- Real-time voltage, current, and power monitoring
- Battery level and temperature display
- Estimated time to full charge/discharge
- Resizable home screen widget
- Material Design UI

Built with Kotlin and Android SDK targeting API 34`;

    try {
      exec(`git commit -m "${commitMessage.replace(/"/g, '\\"')}"`);
      console.log('✓ Initial commit created');
    } catch (error) {
      if (error.message.includes('nothing to commit')) {
        console.log('✓ No changes to commit (already committed)');
      } else {
        throw error;
      }
    }
    console.log('');

    // Step 6: Add remote
    console.log('Step 6: Configuring remote repository...');
    const remoteUrl = `https://github.com/${user.login}/battery-monitor-android.git`;
    
    // Remove existing origin if it exists
    exec('git remote remove origin', { ignoreError: true });
    exec(`git remote add origin ${remoteUrl}`);
    console.log(`✓ Remote 'origin' configured: ${remoteUrl}`);
    console.log('');

    // Step 7: Push to GitHub
    console.log('Step 7: Pushing to GitHub...');
    exec('git branch -M main');
    
    const accessToken = await getAccessToken();
    const authenticatedUrl = `https://${accessToken}@github.com/${user.login}/battery-monitor-android.git`;
    
    try {
      exec(`git push -u origin main --force`, { ignoreError: false });
      console.log('✓ Code pushed to GitHub successfully!');
    } catch (error) {
      // Try with authentication
      exec(`git remote set-url origin ${authenticatedUrl}`);
      exec(`git push -u origin main --force`);
      // Reset to non-authenticated URL for security
      exec(`git remote set-url origin ${remoteUrl}`);
      console.log('✓ Code pushed to GitHub successfully!');
    }
    console.log('');

    // Success message
    console.log('════════════════════════════════════════════════════════════════════════════');
    console.log('  SUCCESS! Your Battery Monitor app is now on GitHub!');
    console.log('════════════════════════════════════════════════════════════════════════════');
    console.log('');
    console.log(`📦 Repository: https://github.com/${user.login}/battery-monitor-android`);
    console.log(`👤 Owner: ${user.login}`);
    console.log(`📝 Description: ${repo.description}`);
    console.log('');
    console.log('Next Steps:');
    console.log('  • View your repository on GitHub');
    console.log('  • Clone it to your local machine:');
    console.log(`    git clone https://github.com/${user.login}/battery-monitor-android.git`);
    console.log('  • Open in Android Studio and build the app');
    console.log('');
    console.log('════════════════════════════════════════════════════════════════════════════');

  } catch (error) {
    console.error('');
    console.error('❌ Error:', error.message);
    if (error.stack) {
      console.error('Stack trace:', error.stack);
    }
    process.exit(1);
  }
}

main();
