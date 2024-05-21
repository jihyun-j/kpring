export type JoinBoxValues = {
  nickname: string;
  email: string;
  password: string;
  passwordConfirm: string;
};

export type JoinBoxValidateErrors = {
  nicknameError: string;
  emailError: string;
  passwordError: string;
  passwordConfirmError: string;
};