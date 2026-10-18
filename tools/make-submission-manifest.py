"""Write a local Plugin Hub marker from real repository/commit values. Never publishes."""
import argparse
from pathlib import Path
import re

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--repository', required=True, help='Public HTTPS GitHub clone URL ending in .git')
parser.add_argument('--commit', required=True, help='Full 40-character SHA of the reviewed source')
args = parser.parse_args()
if not re.fullmatch(r'https://github\.com/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+\.git', args.repository):
    parser.error('Use a public HTTPS GitHub clone URL ending in .git.')
if not re.fullmatch(r'[0-9a-fA-F]{40}', args.commit) or args.commit == '0' * 40:
    parser.error('Use the real full 40-character commit SHA; zero/example values are not valid.')
root = Path(__file__).resolve().parents[1]
target = root / 'build/submission/farm-run-planner'
target.parent.mkdir(parents=True, exist_ok=True)
target.write_text(f'repository={args.repository}\ncommit={args.commit.lower()}\n', encoding='utf-8', newline='\n')
print('Created local marker:', target)
print('Nothing was published. Public visibility and remote commit availability still need verification.')
