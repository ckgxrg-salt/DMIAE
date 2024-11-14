module io.ckgxrg.dmiae.cli {
  requires io.ckgxrg.dmiae;
  requires info.picocli;

  opens io.ckgxrg.dmiae.cli to
      info.picocli;
}
