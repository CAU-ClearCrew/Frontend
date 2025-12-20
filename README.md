## Environment Configuration (Development Only)

For local development and testing purposes,
add the following values to `local.properties`.

> Note: `local.properties` is used **only in the development environment**  
> and is **not included in the production Android application**.

PINATA_JWT="your pinata jwt token"  
DEV_PRIVATE_KEY="your arbitrum sepolia wallet private key"  
RPC_URL=https://sepolia-rollup.arbitrum.io/rpc  
CONTRACT_ADDRESS=0x76a0532BdcF08Ba57177B1F893D6083CA4a83f29

---

## APK File

The APK file (`whistleBox.apk`) is included for demonstration purposes only.

Due to its size, the file is managed using **Git Large File Storage (Git LFS)**  
instead of being tracked directly by Git.

To retrieve the APK:

```bash
git lfs install
git clone https://github.com/CAU-ClearCrew/Frontend.git
```
