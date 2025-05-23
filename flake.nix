{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils, ... }: flake-utils.lib.eachDefaultSystem (system:
    let
      pkgs = import nixpkgs { inherit system; };
    in
    {
      devShells.default = pkgs.mkShell {
        packages = with pkgs; [
          scala
          metals
          scalafmt
          scalafix
          sbt
          clang
          protobuf
        ] ++ (pkgs.lib.optional stdenv.isDarwin [
          pkgs.iconv
          darwin.apple_sdk.frameworks.Foundation
        ]);

        LIBCLANG_PATH = "${pkgs.libclang.lib}/lib";
      };
    });
}
