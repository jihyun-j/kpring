import { useNavigate } from "react-router";
import { RiLoginBoxFill } from "react-icons/ri";
import { useLoginStore } from "../../store/useLoginStore";
import { login } from "../../api/user";
import FormField from "../molecules/FormField";
import Button from "../atoms/Button";
import { useLoginValidator } from "../../hooks/user/useLoginValidator";

const LoginForm = () => {
  const navigate = useNavigate();
  const { setTokens } = useLoginStore();

  const { values, setValues, errors, validateFieldAndSetError, isFormValid } =
    useLoginValidator();

  const onChangeHandler =
    (field: keyof typeof values) =>
    (event: React.ChangeEvent<HTMLInputElement>) => {
      const value = event.target.value;
      setValues((prevValues) => ({ ...prevValues, [field]: value }));
      validateFieldAndSetError(field, value);
    };

  const clickSubmitHandler = async (e: React.FormEvent) => {
    e.preventDefault();

    if (isFormValid()) {
      const result = await login(values.email, values.password);

      if (result.refreshToken) {
        setTokens(result.accessToken, result.refreshToken);
      }

      setTimeout(() => {
        navigate("/");
      }, 3000);
    } else {
      console.log("");
    }
  };
  return (
    <form onSubmit={clickSubmitHandler}>
      <FormField
        value={values.email}
        label="EMAIL"
        type="email"
        onChange={onChangeHandler("email")}
        message={errors.email}
      />
      <FormField
        value={values.password}
        label="PASSWORD"
        type="password"
        onChange={onChangeHandler("password")}
        message={errors.password}
      />

      <Button color="" onClick={clickSubmitHandler} icon={<RiLoginBoxFill />}>
        로그인
      </Button>
    </form>
  );
};

export default LoginForm;
