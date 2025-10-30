#!/bin/bash

set -e

echo "════════════════════════════════════════════════════════════════════════════"
echo "  Setting up Git and Pushing to GitHub"
echo "════════════════════════════════════════════════════════════════════════════"
echo ""

# Step 1: Create the GitHub repository
echo "Step 1: Creating GitHub repository..."
node create-github-repo.mjs

if [ $? -ne 0 ]; then
    echo ""
    echo "❌ Failed to create GitHub repository. Exiting."
    exit 1
fi

echo ""
echo "Step 2: Initializing Git repository..."

# Initialize git if not already initialized
if [ ! -d ".git" ]; then
    git init
    echo "✓ Git repository initialized"
else
    echo "✓ Git repository already initialized"
fi

# Configure git user if not set
if [ -z "$(git config user.name)" ]; then
    git config user.name "Replit User"
    echo "✓ Git user.name configured"
fi

if [ -z "$(git config user.email)" ]; then
    git config user.email "user@replit.com"
    echo "✓ Git user.email configured"
fi

echo ""
echo "Step 3: Adding files to Git..."
git add .
echo "✓ Files added to staging"

echo ""
echo "Step 4: Creating initial commit..."
git commit -m "Initial commit: Battery Monitor Android App

Features:
- Real-time voltage, current, and power monitoring
- Battery level and temperature display
- Estimated time to full charge/discharge
- Resizable home screen widget
- Material Design UI

Built with Kotlin and Android SDK targeting API 34"

echo "✓ Initial commit created"

echo ""
echo "Step 5: Getting repository information..."

# Extract the repository owner and name from the created repo
# We'll read it from git config after setting the remote
REPO_OWNER=$(git config user.name)
REPO_NAME="battery-monitor-android"

echo ""
echo "Step 6: Adding remote repository..."
# Check if remote already exists
if git remote get-url origin &> /dev/null; then
    echo "⚠️  Remote 'origin' already exists. Removing it..."
    git remote remove origin
fi

# This will be set by getting the authenticated user
# For now, we'll use a placeholder and update it via the script
echo "Remote will be configured..."

# We need to get the actual GitHub username
# Let's use a different approach - create a temporary file with repo info
if [ -f ".repo-info" ]; then
    source .repo-info
    git remote add origin "https://github.com/$GITHUB_OWNER/$GITHUB_REPO.git"
    echo "✓ Remote 'origin' added: https://github.com/$GITHUB_OWNER/$GITHUB_REPO.git"
else
    echo "⚠️  Repository info not found. Will set up remote manually."
    echo "Please run: git remote add origin <your-repo-url>"
fi

echo ""
echo "Step 7: Pushing to GitHub..."
if [ -f ".repo-info" ]; then
    git branch -M main
    git push -u origin main
    echo "✓ Code pushed to GitHub successfully!"
    
    echo ""
    echo "════════════════════════════════════════════════════════════════════════════"
    echo "  SUCCESS! Your Battery Monitor app is now on GitHub!"
    echo "════════════════════════════════════════════════════════════════════════════"
    echo ""
    echo "Repository URL: https://github.com/$GITHUB_OWNER/$GITHUB_REPO"
    echo ""
    echo "You can now:"
    echo "  • View your repository on GitHub"
    echo "  • Clone it to your local machine"
    echo "  • Open it in Android Studio"
    echo "  • Share it with others"
    echo ""
    
    # Clean up
    rm -f .repo-info
else
    echo ""
    echo "⚠️  Remote not configured. Please manually push your code:"
    echo "  1. Go to GitHub and get your repository URL"
    echo "  2. Run: git remote add origin <your-repo-url>"
    echo "  3. Run: git branch -M main"
    echo "  4. Run: git push -u origin main"
fi

echo "════════════════════════════════════════════════════════════════════════════"
