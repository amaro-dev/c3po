# Wiki Sync on Release - Documentation

## Overview

This GitHub Actions workflow automatically syncs documentation from `docs/wiki/` to your GitHub Wiki whenever a new
release is published.

## How it works

1. **Trigger**: Runs when a release is published (not drafts or pre-releases)
2. **Source**: Copies all `.md` files from `docs/wiki/` directory
3. **Destination**: GitHub Wiki repository (`.wiki.git`)
4. **Strategy**: Full replacement sync (clears wiki, then copies all files)

## Prerequisites

1. **Wiki feature enabled**: Go to Settings → Features → Check "Wikis"
2. **Wiki content**: At least one `.md` file in `docs/wiki/` directory
3. **Permissions**: Workflow has `contents: read` and `pages: write` permissions

## Testing the Workflow

### Option 1: Manual Trigger (Recommended) 🧪

The workflow includes a manual trigger for easy testing:

1. **Go to Actions tab** in your GitHub repository
2. **Select** "Sync Wiki on Release" workflow
3. **Click** "Run workflow" button
4. **Configure test options**:
    - **Test mode**: Check this box ✅
    - **Test version**: Enter something like `v1.0.0-test`
5. **Click** "Run workflow"

**Benefits**:

- ✅ No need to create releases for testing
- ✅ Uses test commit messages
- ✅ Can run multiple times safely
- ✅ Clear test indicators in logs

### Option 2: Create a Test Release

```bash
# Create and push a test tag
git tag v0.0.1-test
git push origin v0.0.1-test

# Then create a release from this tag on GitHub
```

### Option 3: Branch Testing

Create a test version on a separate branch:

1. Copy the workflow file
2. Change trigger to `push: branches: [test-wiki-sync]`
3. Test on that branch
4. Remove test trigger before merging

## Workflow Behavior

### ✅ Success Cases

- **Wiki updated**: Files synced successfully
- **No changes**: Wiki already up to date
- **Clear logging**: Shows exactly what files were processed

### ⚠️ Warning Cases

- **No content**: `docs/wiki/` directory empty or missing
- **Wiki disabled**: Feature not enabled in repository settings

### ❌ Error Cases

- **Authentication failed**: GITHUB_TOKEN lacks permissions
- **Network issues**: Git operations fail (includes retry logic)
- **Wiki conflicts**: Rare git conflicts during push

## File Handling

### Supported Files

- **Source**: `docs/wiki/*.md` (recursive)
- **Destination**: Wiki root directory
- **Naming**: Preserves original filenames

### Special Files

- `Home.md` → Wiki home page
- `_Sidebar.md` → Wiki sidebar
- Other `.md` files → Regular wiki pages

### Not Supported

- Binary files (images, PDFs) - wiki doesn't support them directly
- Nested directories - files are flattened to wiki root
- Non-markdown files - only `.md` files are synced

## Commit Messages

Generated commit messages include:

- Release version and name
- Release URL for reference
- Source commit SHA
- Timestamp

Example:

```
📚 Sync wiki content for release v1.2.3

Updated wiki documentation from docs/wiki/ as part of release "Feature Update".

Release: https://github.com/user/repo/releases/tag/v1.2.3
Commit: abc123def456
Timestamp: 2024-01-15 14:30:00 UTC
```

## Troubleshooting

### Wiki repository not found

1. Enable Wiki feature in repository settings
2. Create at least one wiki page manually to initialize repository
3. Re-run the workflow

### Permission denied

1. Check repository settings → Actions → General
2. Ensure "Read and write permissions" is enabled for GITHUB_TOKEN
3. Or check if organization has restricted permissions

### Files not syncing

1. Verify files exist in `docs/wiki/` directory
2. Check files have `.md` extension
3. Review workflow logs for specific errors

### Network timeouts

The workflow includes retry logic (3 attempts) for git operations. If it still fails, check GitHub status or try
re-running the workflow.

## Maintenance

### Regular Tasks

- Monitor workflow runs in Actions tab
- Review wiki content after releases
- Update this documentation as needed

### Updating the Workflow

1. Test changes on a separate branch first
2. Use manual trigger for testing
3. Monitor logs carefully for new issues

## Security Notes

- Uses GitHub's built-in GITHUB_TOKEN
- No external secrets required
- Only accesses wiki repository
- Minimal permissions (read repo, write wiki)