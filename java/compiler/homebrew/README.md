
# Homebrew package

This folder contains a command line script template `present-rpc` for running the rpc compiler via homebrew.
The script and present-rpc-compiler.jar file are zipped, named by version, and placed into the archives folder to be referenced by the `present-rpc.rb` homebrew formula hosted on our repository `presentco/homebrew-util`.

## Creating a new version

Update the version number in create-archive.sh and run the script.
Update the `present-rpc.rb` file in the `presentco/homebrew-util` repository to reference the new archive version.

