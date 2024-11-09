{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
  };

  outputs = { nixpkgs, ... }:
  let
    system = "x86_64-linux";
    pkgs = import nixpkgs {
      inherit system;
    };
  in {
    devShell.${system} = pkgs.mkShell {
      name = "java-dev";

      buildInputs = with pkgs; [
        jdk
        google-java-format
        checkstyle
      ];
      
      shellHook = ''
        exec nu
      '';
    };
  };
}
