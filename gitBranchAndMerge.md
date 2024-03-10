# Branching and Merging

> This advice is given by ChatGPT 3.5

## Evolving your code in another branch

In software development, branching is a common practice, especially when working with version control systems like Git. Here's a general guide on how to create a new branch, make changes, and merge those changes back into the main branch.

### Creating a New Branch:

1. **Checkout the Main Branch:**
   Before creating a new branch, make sure you are on the main branch. You can use the following command in Git:
   ```bash
   git checkout main
   ```

2. **Create a New Branch:**
   To create a new branch, use the following command:
   ```bash
   git checkout -b new-feature
   ```
   This creates a new branch named `new-feature` and switches to it.

3. **Make Changes:**
   Make your changes in the new branch. Commit your changes regularly as you progress.

### Updating the Main Branch:

4. **Switch Back to the Main Branch:**
   Once your changes in the new branch are complete, switch back to the main branch:
   ```bash
   git checkout main
   ```

5. **Pull the Latest Changes:**
   It's a good practice to pull the latest changes from the main branch to ensure your local main branch is up-to-date:
   ```bash
   git pull origin main
   ```

6. **Merge Changes:**
   Merge the changes from your feature branch into the main branch:
   ```bash
   git merge new-feature
   ```
   Resolve any merge conflicts if they occur.

7. **Push Changes:**
   Finally, push the changes to the main branch on the remote repository:
   ```bash
   git push origin main
   ```

### Alternative: Pull Request (GitHub, GitLab, Bitbucket, etc.):

Alternatively, if you are using a platform that supports pull requests (e.g., GitHub, GitLab, Bitbucket):

1. **Create a Pull Request:**
   Push your changes to the new branch and create a pull request (PR) to merge those changes into the main branch.

2. **Review and Merge:**
   Your team or collaborators can review the changes in the pull request. Once approved, you can merge the changes into the main branch.

This approach allows for a more structured code review process and is often used in collaborative development environments.

Remember to adapt these steps based on your specific development environment and version control system.

## Continue development in feature branch

It's generally a good practice to synchronize your feature branch with the main branch before continuing development in that branch. This ensures that your feature branch includes the latest changes from the main branch and helps prevent potential conflicts.

Here's a recommended sequence of steps:

1. **Checkout the Feature Branch:**
   ```bash
   git checkout new-feature
   ```

2. **Pull Latest Changes from Main:**
   Pull the latest changes from the main branch into your feature branch:
   ```bash
   git pull origin main
   ```

3. **Resolve Conflicts (if any):**
   If there are any merge conflicts, Git will prompt you to resolve them. Resolve the conflicts, commit the changes, and continue.

4. **Continue Development:**
   Now that your feature branch is up-to-date with the latest changes from the main branch, you can continue your development work.

By synchronizing your feature branch with the main branch regularly, you reduce the chances of encountering conflicts later on and make it easier to integrate your changes back into the main branch when you're ready.

Remember that the specific commands may vary slightly depending on your version control system and the platform you're using (Git, GitHub, GitLab, Bitbucket, etc.), so adapt the steps as needed based on your development environment.

## Reset feature branch to main branch

When you encounter the message "fatal: Not possible to fast-forward, aborting." during a `git pull`, it indicates that there are changes in your local branch that are not present in the remote branch, and vice versa. This situation can happen if both branches have diverged, meaning each has commits that the other doesn't have. To resolve this and force your local `development` branch to exactly match the remote `main` branch, you can use the following steps:

1. **Ensure you are on the correct branch**: 
   First, make sure you are on your `development` branch.
   ```bash
   git checkout development
   ```

2. **Fetch the latest changes from the remote repository**: 
   This step doesn't change your local code but updates your local knowledge of the remote repository.
   ```bash
   git fetch origin
   ```

3. **Reset your branch to match the remote branch**: 
   Use the `reset` command to align your local branch with the remote `main` branch. This will overwrite your local changes to match the remote branch.
   ```bash
   git reset --hard origin/main
   ```

   The `--hard` flag tells Git to overwrite all changes in your working directory and index. Be cautious with this command: any local changes that are not committed will be lost.

4. **Push the changes to your remote `development` branch (if needed)**: 
   If your `development` branch also exists on the remote and you want to update it to reflect the state of `main`, use the following command:
   ```bash
   git push -f origin development
   ```

   The `-f` flag stands for "force". This force-pushes your changes to the remote repository. Be cautious with this step, especially if others are working on the same branch, as it can overwrite changes they've made.

Remember, these actions can lead to loss of committed data on your `development` branch and potentially disrupt collaboration if others are working on the same branch. Always ensure that you have a backup of your current work or have confirmed that overwriting is safe in your collaborative context.