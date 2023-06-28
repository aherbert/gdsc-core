/*-
 * #%L
 * Genome Damage and Stability Centre Core Package
 *
 * Contains core utilities for image analysis and is used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2023 Alex Herbert
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package uk.ac.sussex.gdsc.core.utils.rng;

import org.apache.commons.rng.RandomProviderState;
import org.apache.commons.rng.RestorableUniformRandomProvider;
import org.apache.commons.rng.core.util.NumberFactory;
import uk.ac.sussex.gdsc.core.data.VisibleForTesting;

/**
 * Middle Square Weyl Sequence Random Number Generator.
 *
 * <p>A fast all-purpose 32-bit generator. Memory footprint is 192 bits and the period is at least
 * {@code 2^64}.</p>
 *
 * <p>Implementation is based on the paper <a href="https://arxiv.org/abs/1704.00358v3">Middle
 * Square Weyl Sequence RNG</a>.</p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Middle-square_method">Middle Square Method</a>
 */
public final class MiddleSquareWeylSequence
    implements RestorableUniformRandomProvider, SplittableUniformRandomProvider {
  /**
   * Example increments for the generator. These consist of 1024 increments; each increment is
   * permutation of 8 hex characters. All increments and their byte reversed versions are unique.
   * This results in 2048 example permutations.
   */
  private static final int[] INCREMENTS = {0x8b5ad4ce, 0x64d098b5, 0x45973acb, 0x6e9c5db1,
      0x4fa75198, 0x3d4c562e, 0xd8b57104, 0x2bdfe60a, 0x86ced140, 0x7c981fa6, 0xe5f68a3c,
      0x8e46fc02, 0x02c34bf7, 0xcebf7a1d, 0x4bf18673, 0xcb5970f8, 0x5bf739ad, 0x68cae093,
      0xf1d465a8, 0xf713c8ed, 0x47890a62, 0x86342a07, 0xf01527dc, 0x762c03e1, 0x0578be16,
      0x24fb567a, 0xa1b3cd0f, 0xc98a21d4, 0xdfb654c8, 0xdb2065ec, 0x836b0541, 0xd0e762c5,
      0x5621ce79, 0x012a385d, 0x62fac071, 0x47a806b5, 0x568c7b29, 0x593842cd, 0x928c3ea0,
      0x8637cda4, 0x79e26ad8, 0x15c2e63b, 0x0a5d93cf, 0x1b045792, 0x902a7d85, 0x4cd5f1a8,
      0xec4d53fb, 0xf281047e, 0xd8eca931, 0x8325a014, 0xe61f4b27, 0xfce1746a, 0x90647bdc,
      0x5ab2617f, 0xc9b4f251, 0xd821c754, 0xd12a4786, 0xcd46a5e8, 0x63dce27b, 0x9a586f3d,
      0x791c862f, 0xe0f7cd51, 0x90d7c8a2, 0xc03a1624, 0x420187d6, 0x906ed7b8, 0x3b1205c9,
      0x78a6120b, 0xd2b59f7c, 0x79bec51d, 0xc7196bef, 0x8da1e9f0, 0xade05421, 0x76ec9582,
      0x6a4eb513, 0x07c6b2d4, 0x2a048ec5, 0xd13748e5, 0x0f92e136, 0xf8c015b7, 0x0b91ac67,
      0xe361df48, 0x69e2f058, 0x5e7cdf98, 0xe4b6ad08, 0x3e1695a8, 0x4901e278, 0xf1904a78,
      0x215490a8, 0x1d4eb508, 0xdc5eb798, 0xf02e4958, 0x1fa80547, 0x0e51f467, 0x83059fb6,
      0x0af6c935, 0x1b97a0e5, 0x57b316c4, 0xce920ad3, 0x754a6d12, 0xb736fd81, 0x9e571c20,
      0x491028ef, 0xca7143ed, 0x9a36fd1c, 0xd98f047b, 0x851cea09, 0x6be0adc8, 0x20da4fb6,
      0xb109cfd4, 0x903d6fe2, 0x57d806a1, 0x9adb854f, 0x3a4b7e2d, 0x0691f53b, 0x53920a78,
      0x183a9de6, 0xca190f84, 0xeb82df51, 0xcb278d4f, 0x3d48f97c, 0x05cb83da, 0x1ba94c67,
      0xc5baf324, 0x6e3d2781, 0x109fdb2e, 0xa0631c7b, 0x105c3bf8, 0xf48b39a5, 0xdaf01582,
      0x1039ac8f, 0xa49567cb, 0x7f61de38, 0xfc9e32d4, 0xc29165a0, 0xe3b8769d, 0x23ad65c9,
      0x0befa325, 0x05f3deb1, 0xef47386d, 0xaf28d059, 0xfbe41675, 0xf7953ac1, 0x16f98d3c,
      0xf7491de8, 0xa5e9bdc4, 0xa3d479cf, 0x1e62f50a, 0xb5fc4e76, 0xabe50861, 0x26a19bdc,
      0x51ba8fd7, 0x54813602, 0x4f98125d, 0xd72ca0e8, 0xbe890da3, 0x3e27158d, 0xfd4561a8,
      0x0c7e58f3, 0xc2b9ae6d, 0xb9853217, 0x14a5d3f2, 0xd67b53fc, 0x15f8b236, 0x53bf4ea0,
      0x16b4093a, 0xdf371204, 0x5c74d8fe, 0x5b416e28, 0xfc53e281, 0xd2e4930b, 0x0f37e2c5,
      0x834f10ae, 0x8af291c7, 0x562c4071, 0x4e26cf8a, 0x814cf763, 0x534dfb0c, 0xa8c02e15,
      0x781c9f4e, 0x9480ceb7, 0x9762bc50, 0x4a72e819, 0x93ab0521, 0x89517e3a, 0xaed56092,
      0x0ed3851b, 0xcf41e7d3, 0xc24a0f8b, 0xc9a5e7d3, 0x3c6e051b, 0xb4516093, 0x65f7ea3b,
      0xa7d45213, 0x25e6981b, 0x702ebc53, 0x3176ceba, 0x34609f52, 0x284a5e19, 0x58369fb1,
      0x5abf7638, 0x12e4ac8f, 0xdf5ec017, 0x95a031ce, 0xf92310b5, 0x84d69e2c, 0xcf72b913,
      0xd3fa2189, 0xb6cf8e30, 0xc238d907, 0x573a920d, 0x85a1f094, 0x9cf4b2ea, 0x973cb241,
      0xf1385407, 0xc52013fd, 0x789cb523, 0x4fb36e79, 0xc176bd8f, 0xc96281b5, 0x6e1d589b,
      0x395e0db1, 0x9d54a0f6, 0x49d8126c, 0x489e3621, 0xc12b8fe7, 0x28749bec, 0xa74d8621,
      0xba574e86, 0x42e6f51c, 0x534c79e1, 0x4e217cd6, 0x74891dfa, 0x71e03d4f, 0x6c753ad4,
      0xfa701689, 0x2bce806d, 0xab907682, 0x895bdec6, 0x859ce63b, 0x7bca65df, 0x589c76b3,
      0xd0c865b7, 0xf85a32eb, 0xb5c1de4f, 0x428f67e3, 0x8b92cfa7, 0xd30c159b, 0x6f5239be,
      0x7fe03c12, 0xe60b1c95, 0x30fcdb49, 0x4eb1782c, 0x2d408e3f, 0xedb14c83, 0x7d2983f6,
      0xf0bd5479, 0x549b8d6c, 0x8730256f, 0xc7e50fa2, 0xcbda9e04, 0x32c60a97, 0xc018675a,
      0x280f7e4c, 0x04cb856f, 0x942e6ac1, 0xfa072e43, 0x092d7cf5, 0xb31a4fd8, 0x0c2f4dea,
      0xf304ea2c, 0xa3db049e, 0x850c6d3f, 0xf0678d41, 0xc5608913, 0x6d07e1c4, 0x4f718da6,
      0xf2b5ad37, 0x5db10af9, 0x570f16ea, 0x47f3a10b, 0x67d3a95c, 0x74316fde, 0x6b90d48f,
      0xb2a0576f, 0xd26f3980, 0xd0a6b7c1, 0xde4ab532, 0xa68490d2, 0xc9b6e4a3, 0xf079e2a3,
      0x61ef58d4, 0x0c96ad34, 0x9608dfc4, 0x9d3af084, 0x8bc2df74, 0x2e86ac94, 0x9f1657e4,
      0x7a39e164, 0x3f5a6914, 0x5b6c8ef4, 0x78f5ab03, 0x0e26b543, 0x6f0895b2, 0xb9f60452,
      0x86bcdf21, 0xf3716c20, 0x2d3ac54f, 0x0e159caf, 0xcb9f123e, 0x621a05fc, 0x14cad7eb,
      0x9e3b180a, 0xd8ce1659, 0x4b6082d7, 0xb59acd86, 0xda879f64, 0x015afd73, 0xcd84e2b1,
      0x5ce4a61f, 0x9d2a47be, 0x2af4578c, 0x9647258a, 0xcd7e61b8, 0x8feb7c15, 0x6c8e74a3,
      0xdf374b61, 0x2dc7604f, 0x2eba936c, 0x7d3504ba, 0x6a2e5437, 0x73cb81e4, 0x31e78dc2,
      0x0d1397cf, 0xf38a140c, 0xba04e679, 0x4d872b16, 0x289bcde3, 0x87a4fbe0, 0x53d92e0c,
      0x71052b69, 0xb43c06f5, 0x4e79c0b2, 0x6a3f589e, 0x45fa1ceb, 0x8e1a2307, 0xb2dcf583,
      0xe509c62f, 0x7da4650b, 0x03cf6217, 0x761bcd53, 0xd2c756bf, 0x96148e5b, 0xf17d0426,
      0xcafb7d82, 0xc3802a4d, 0x5734b0a9, 0x2507c934, 0xd09c65ef, 0x87c6e0db, 0xde4739f6,
      0xe3b9d741, 0xad0986bc, 0xe829ba67, 0xa2083c41, 0x2ea90f4c, 0xb3cd4a87, 0x52b4d6f1,
      0x75fd418c, 0x39ce2a56, 0x93afd751, 0xcf4e567b, 0x4eca19d5, 0x03291b5f, 0xd6843b19,
      0x576ac903, 0x8b30a51d, 0xc8b9ef67, 0x5c0687e1, 0x3716be8a, 0x059e7364, 0x2b48a06d,
      0x4e06dba7, 0xbf82c710, 0xb2d8f4a9, 0x516cd073, 0x2d4bea6c, 0x70fab295, 0x137df58e,
      0xc10af9d7, 0xbe12a430, 0x6e95d018, 0x958ca031, 0x5f1e9d7a, 0x09a678f2, 0x7e04329a,
      0x26e9ca73, 0xad61407b, 0x2c6094b3, 0xf295c71b, 0x5c20d7b4, 0x938a167c, 0xc5428973,
      0x2c853e9b, 0xe06ac7f3, 0x89402f7b, 0x0a8e7532, 0xb6cd291a, 0x8dec9b31, 0x2ed3cb79,
      0xb8c239f0, 0x14c0d697, 0xb74f516e, 0xfd496a75, 0x50f3e1ac, 0xb986f713, 0x8c714fea,
      0x3f4ebc71, 0xe01d3c68, 0x2c5dfa8e, 0x3d4b86e5, 0xec9d416b, 0x9b70da21, 0x35b2e108,
      0x3b2ac61e, 0xcbed8964, 0x493bc2da, 0x163d5a80, 0xadb20856, 0x9d73a45c, 0xd1365e92,
      0x183056f7, 0xf456728d, 0x4bd9e253, 0xba6c7548, 0xf8934e6d, 0x649e25c3, 0x9db76348,
      0x40816efd, 0xd67158e2, 0xd39420f7, 0x1e65273c, 0xad2894b1, 0xf70bae56, 0x31b8ef2a,
      0xd89c0e2f, 0xea950b64, 0x4b03e6c8, 0x3e68a05d, 0x09e43821, 0x6b23ae15, 0x4b5a0239,
      0x5eb6348d, 0x29078451, 0x9be103c5, 0x12bde0a9, 0xc3541abd, 0x64ab3501, 0x30ea9c74,
      0x6cbfe218, 0x6c9205eb, 0x436c07ef, 0xcd139e82, 0xdecfa685, 0x9b724318, 0x94068adb,
      0x2ed916cf, 0x7ae54df1, 0xb5917634, 0x48d056c7, 0xc65d287a, 0xfeb9d85c, 0x40381a6f,
      0x896ed2b1, 0x089f1d24, 0xae1245c6, 0xa67b4c98, 0x5e4a319b, 0x319ef4cd, 0x80d3962f,
      0xf863a5c1, 0x954b0783, 0xe95caf74, 0xb483ec96, 0x2956c1e8, 0xf1ba4869, 0x3e284d1b,
      0xa4be1dfc, 0x98fd510e, 0x7d96a04f, 0x86251dc0, 0x34e2d961, 0x85ba9c32, 0x974a68b3,
      0xf7d93164, 0xd0341bc5, 0x4ae01856, 0x08b45916, 0x596be807, 0x490fe527, 0xa6b45078,
      0x63a209f8, 0x57b901a8, 0x0d549178, 0x03d7c6b9, 0x82760ed9, 0xc0b2ae49, 0x5ba19ce8,
      0xcf0269b8, 0xadc714b8, 0x07a19de8, 0x3c680547, 0xe9f64ad7, 0x1f0b2e96, 0xa3fe7086,
      0x3419c0a5, 0x59b70ef4, 0x5619ab73, 0x5a83b962, 0x352e7f01, 0x3cd9b610, 0x5d3acb4f,
      0x0536d1be, 0x761e905c, 0xd8a1402b, 0xa305ce29, 0x90d43a58, 0x3fdc84b6, 0x089c6ad4,
      0xe256b403, 0x2a7c98f1, 0xe6185c0f, 0x86349f5d, 0x96e17cdb, 0xc34eda89, 0x9bec0216,
      0xde2b3074, 0x93e6a8b2, 0xa934de1f, 0xf67893bd, 0xfb13468a, 0x56e10b87, 0x58cd06b4,
      0xb8cf3412, 0xe4c0739f, 0x087f295c, 0xde50f148, 0x8e429765, 0xdfe501b2, 0x351c7e2f,
      0x0fac2edb, 0x291f3db8, 0xe815dac4, 0xdef7b601, 0x478caf6d, 0xf8ed3709, 0x4f217cd5,
      0x42f850d1, 0x8ba302fd, 0x8dc01359, 0x742601e5, 0x5437cea1, 0xbef3798d, 0xe76b02a8,
      0x12a869f4, 0x7d91ba6f, 0x6874d31b, 0xb7a3d4f6, 0xd468b501, 0x21b5673c, 0x12940fa7,
      0x6ebf8a42, 0x4196e30d, 0xb3691a08, 0xc46712f3, 0x6dbaf28e, 0xad72f418, 0x20f4cad3,
      0x87ac31bd, 0x62ec19d8, 0x1960ce82, 0xba8e109c, 0x82951746, 0xa531fc20, 0x1d54bf2a,
      0xec1ad064, 0xa23bdfce, 0x17f03d68, 0x48fa7932, 0x85da932b, 0xc3e908b5, 0xb92c61ae,
      0xd5fe1638, 0x6c05a8f1, 0x5e4319da, 0x1dcb68f3, 0xd75f963c, 0x743ea1b6, 0x94138b5e,
      0xab09e537, 0x7c1ef940, 0xe4035d79, 0x957cdfe1, 0xb5e4207a, 0xba0c3f42, 0x0487ac3b,
      0x958e1763, 0x3a287d0b, 0x13fb7684, 0x24b91dfc, 0xf7b831e4, 0xb08963fc, 0xc8fe9074,
      0x958cd62b, 0x9b402f63, 0xc1e8da3b, 0x50c76342, 0x13edbc7a, 0xa3260fe1, 0x524a6378,
      0xae6c3540, 0x02481537, 0xfa69d35e, 0xaec16fb5, 0x80f4ea3c, 0x85c142f3, 0x850b79da,
      0xcd3a8ef0, 0x5b9f8237, 0x162953ae, 0x78db0354, 0xa35c912b, 0xa8b4fd31, 0xe29c0467,
      0xb9486fcd, 0xd05b4763, 0x3dea5b29, 0x7bda4e1f, 0x1923bf45, 0x2f0d3e9b, 0x8a749c21,
      0x014e57d6, 0x9063f1bc, 0x358ce9d2, 0x36e8c017, 0x62f5748c, 0x6cb40732, 0xc5261807,
      0xf45b270c, 0x50d82f41, 0x5d3b8fa6, 0x05c4e93b, 0xfec36b10, 0xcade56f4, 0x37ecdb19,
      0x28cf3d6e, 0xc0187df2, 0x5d139ca7, 0x1234c98b, 0x80a3749f, 0xb7582de3, 0xd3baec57,
      0xe87b3afc, 0xa6bf8ed0, 0xb1f5c0d3, 0xa923d107, 0x95187f6b, 0xec6218bf, 0x40f136c2,
      0x8ecd5fb6, 0xcb8326d9, 0x01a36c2d, 0x914a8fb0, 0x48279163, 0xfb3a7146, 0x6a832f59,
      0x53601b9c, 0xc7db460f, 0x41a09eb2, 0x6b1c0d85, 0x4316ea87, 0xce762dba, 0x2b64af1c,
      0xb02c5eaf, 0xf089ec71, 0x32ed5864, 0x593da286, 0x93f5cad8, 0xf298d15a, 0x3295b60c,
      0xf490678e, 0xc75bd1a0, 0x30ea8941, 0x4759f6b3, 0x97ce4325, 0x7a804c26, 0xd7016428,
      0x3fc01a59, 0x741fceba, 0xb875614c, 0xe16b920d, 0x915c20fe, 0xa2b94e1f, 0x13e65970,
      0x675942f1, 0x9f5e20a1, 0x1facb082, 0xf82b5493, 0x14ef96d3, 0xca35fd74, 0x1c30f5e4,
      0xa3e1f2b4, 0xa7f3cdb5, 0x273b86e5, 0xf2b91e45, 0x2a6c93d5, 0x1c3de795, 0x64e71985,
      0xd8bc29a4, 0xe63517f4, 0x2a815e74, 0x590c8f23, 0xa4691803, 0x8d9b7f12, 0x80f3c451,
      0x9580e7c1, 0x23f5e960, 0xa713c92f, 0xb5af872e, 0x49a8235d, 0xa7309dbc, 0x8e21f64a,
      0xb7182d09, 0x5d0641f8, 0x7cb93516, 0xde8a2065, 0x2dc0b5e3, 0x68154392, 0x3159af70,
      0x8c25f97e, 0x045621bc, 0xc765082a, 0x571e40c8, 0x137bcf96, 0x2db87094, 0x7694efc1,
      0x09864d1f, 0x27cf58ad, 0xdeb8926a, 0xf3c19a57, 0x9d486075, 0x50a3d4c2, 0x2c49b73f,
      0x0a6f27ec, 0x1ae476c9, 0x7b02a3d6, 0xd756af13, 0x53bfe980, 0x784a601d, 0x8fc605e9,
      0x02c189e6, 0x7350ec12, 0xd1ba2c6f, 0x13874afb, 0x6318a4b7, 0x5ebc32a4, 0xa231dbc0,
      0x94d6730c, 0xf743b0e8, 0x958e1c34, 0x92576e0f, 0xd9837e1b, 0x2b936c57, 0x751d38c2,
      0x04cda35e, 0xa18e6c29, 0x4f17d325, 0xbed71850, 0x1607c3ab, 0x12cf7d36, 0x40e58cf1,
      0x852beadc, 0x9105b6f7, 0x73bdf142, 0xcef649bd, 0xa9c48067, 0xc7089542, 0x36de284c,
      0x30f25987, 0x267308f1, 0x1043968b, 0x9be30256, 0x9ef74c50, 0xb286947a, 0x93bc7ad4,
      0xab1c4f5e, 0x6bd04217, 0x5af8c301, 0xc9f3421b, 0xb1839f64, 0x23019ade, 0x31e5f487,
      0xf9d7ec61, 0xd81fc26a, 0xbe8d76a3, 0xad23190c, 0x42d079a5, 0x5b19c86e, 0xc32ef567,
      0x7c85da90, 0x2fc58ae9, 0x9d40ab71, 0x698e572a, 0x7ae0db13, 0x1e693d2b, 0x05ab2d73,
      0xd6419bec, 0xa27f5894, 0x94a5736c, 0xa83b2c74, 0xdf2063ac, 0xec083914, 0x90e3f8ac,
      0x08acbe73, 0xa34fce6b, 0x7a28bc93, 0x94d378ea, 0xd4b5c372, 0xe7f6bc29, 0xefa72310,
      0x5173d628, 0x73a98b6f, 0x20fb8ce6, 0xa1e36c8d, 0xf5d12a64, 0xf9e34c6b, 0xcb5e40a1,
      0xea4bd908, 0x40a52c9f, 0xac1de465, 0x4a1ed75c, 0x39645a82, 0xfac257d8, 0x1ad64e5f,
      0xa49c3d15, 0xe6749afb, 0x4ae8fc31, 0xaf20c957, 0x561b9acd, 0x16d9f072, 0x06a5b148,
      0x65ac904e, 0x16a9ed83, 0x375128e9, 0xf360427e, 0xb056da43, 0xb26c1039, 0x3960c45e,
      0xade256b3, 0x542bc738, 0x486215ed, 0x081ce4d2, 0x1f704de6, 0xa648372b, 0x2495fea0,
      0xd63289a4, 0x5c432819, 0x9bc28a1d, 0xe8f7ca51, 0x4fc2e8b6, 0xfc13e54a, 0xb549ac0e,
      0xfe357902, 0x794a1026, 0xc9f2857a, 0xba3608fd, 0xec650ab1, 0xe82f1a95, 0xbc62f0a8,
      0xb106d4ec, 0xa46d075f, 0x41972803, 0xb3849ed6, 0x7156b3d9, 0x84a9d70c, 0xae12d86f,
      0xadc1b802, 0x2d9a67c5, 0x0da2c1b8, 0x32618bda, 0x3697e42d, 0x63541ab0, 0xa7dc4f62,
      0xdaf16e24, 0xa2cbf357, 0xeb05fa29, 0xe4a2930b, 0x40289bad, 0x9c2de57f, 0x27c90d81,
      0x74e6a1b3, 0x3240f815, 0xec54dba7, 0xa4905b68, 0x1c608d5a, 0x9c25e37b, 0x649b72cd,
      0x72b58c4e, 0xeb60583f, 0x608b59e1, 0xb5470df2, 0x2e168a03, 0x5d3610a4, 0x19cbdf45,
      0x6e208c15, 0xeda49716, 0xce218047, 0xf09cd4a7, 0xf2b7ed38, 0xb2d570f8, 0x80b4d2e9,
      0x65fb1309, 0x867d3159, 0x84ef52d9, 0xdc4a3089, 0xbe58c169, 0xfda05879, 0x31efcdb9,
      0x0da75129, 0xbf3052c8, 0x4e316298, 0xbd485097, 0x0bea51c7, 0x8193c726, 0x93604fb5,
      0x9afeb675, 0x9e32fb64, 0x0c9d1e83, 0xa0731fd2, 0xb64e2f51, 0x7241ebcf, 0x1a6058de,
      0x148b72ed, 0xfcd3852b, 0x3e78619a, 0x4bf17638, 0xbc3d6907, 0xe82c3a05, 0xad10fe93,
      0x43207691, 0x98c6e21f, 0x940832bd, 0x4de153cb, 0x47bfd5e9, 0x049dbe37, 0x13c8f0b5,
      0x4e78a162, 0x7e918240, 0xc62fe74d, 0x5c34af8b, 0xe239d1f8, 0x1dfb0c95, 0x185d9f62,
      0xb914035f, 0x7419e58c, 0x41da75e9, 0x9e2ba476, 0xb9ae5d13, 0x6935dc1f, 0x908fb53c,
      0x64a57c89, 0x67e93205, 0xe68d05b1, 0x40ba178e, 0xf6e1579a, 0xf84a75d6, 0x165e9742,
      0x029b4cde, 0x4e7c905a, 0x7d409ca6, 0x497801d2, 0x08fc652d, 0xca30e6b9, 0xf832a674,
      0x81f79460, 0x25f8607b, 0x38b20ac7, 0x6da19342, 0x3ac6f9ed, 0xb1423ec8, 0x829b61d3,
      0xdb7a630e, 0x0acf4279, 0x8b93a014, 0x6a129bde, 0x23cb15d9, 0x4b856e03, 0xd68fa45e,
      0x94dcfbe8, 0x96145ba3, 0x60f17c8d, 0x6cd32ba7, 0xd4eab8f1, 0x0938246b, 0xc39b6e15,
      0xa67495ef, 0x2c863bf9, 0xb6f48032, 0xd5e3429c, 0xcd497e36, 0x80d3a61f, 0x7d36bef8,
      0xd3568fa2, 0x548d137b, 0x59e6fb04, 0x9542e0bd, 0x358c94a6, 0x978c26bf, 0xc5319708,
      0x27ace581, 0xaf38e129, 0x6ea51d02, 0xc1af260b, 0xbf57cd43};

  /** 2^32. */
  private static final long POW_32 = 1L << 32;

  /** State of the generator. */
  private long x;
  /** State of the Weyl sequence. */
  private long weyl;
  /**
   * Increment for the Weyl sequence. This must be odd to ensure a full period.
   *
   * <p>This is not final to support the restore functionality.</p>
   */
  private long inc;

  /**
   * Create a new instance.
   *
   * @param state the state
   * @param weylState the weyl state
   * @param increment the increment
   */
  @VisibleForTesting
  MiddleSquareWeylSequence(long state, long weylState, long increment) {
    x = state;
    weyl = weylState;
    // Ensure the increment is odd to provide a maximal period Weyl sequence.
    inc = increment | 1L;
  }

  /**
   * Create a new instance.
   *
   * @param seed the seed
   * @return the generator
   */
  public static MiddleSquareWeylSequence newInstance(long seed) {
    final long increment = createIncrement((int) seed);
    // Use the increment for the state as well.
    // This ensures quality random output from the first cycle.
    // Using the seed for the Weyl state ensures a possible 2^64 generators can be created.
    return new MiddleSquareWeylSequence(increment, seed, increment);
  }

  /**
   * Creates the increment.
   *
   * @param seed the seed
   * @return the increment
   */
  private static long createIncrement(int seed) {
    // Create the increment.
    // Use the input seed to compose an increment from example hex permutations.
    // The number of possible raw increments is (1024*2)^2 = 2^22.
    // The lowest bit is then set (to make it odd) leaving approximately 2^21 = 2,097,152.
    int inc1 = INCREMENTS[seed & 0x3ff];
    int inc2 = INCREMENTS[(seed >>> 10) & 0x3ff];
    // Reverse increments to double the possible number.
    // Use the unused parts of the seed bits.
    if (((seed >>> 20) & 1) == 1) {
      inc1 = Integer.reverseBytes(inc1);
    }
    if (((seed >>> 21) & 1) == 1) {
      inc2 = Integer.reverseBytes(inc2);
    }
    return NumberFactory.makeLong(inc1, inc2);
  }

  /**
   * Gets the example seed increments.
   *
   * @return the increments
   */
  @VisibleForTesting
  static int[] getIncrements() {
    return INCREMENTS.clone();
  }

  @Override
  public void nextBytes(byte[] bytes) {
    nextBytes(bytes, 0, bytes.length);
  }

  @Override
  public void nextBytes(byte[] bytes, int start, int len) {
    int index = start; // Index of first insertion.

    // Index of first insertion plus multiple of 4 part of length
    // (i.e. length with 2 least significant bits unset).
    final int indexLoopLimit = index + (len & 0x7ffffffc);

    // Start filling in the byte array, 4 bytes at a time.
    while (index < indexLoopLimit) {
      final int random = nextInt();
      bytes[index++] = (byte) random;
      bytes[index++] = (byte) (random >>> 8);
      bytes[index++] = (byte) (random >>> 16);
      bytes[index++] = (byte) (random >>> 24);
    }

    final int indexLimit = start + len; // Index of last insertion + 1.

    // Fill in the remaining bytes.
    if (index < indexLimit) {
      long random = nextInt();
      for (;;) {
        bytes[index++] = (byte) random;
        if (index < indexLimit) {
          random >>>= 8;
        } else {
          break;
        }
      }
    }
  }

  @Override
  public int nextInt() {
    x *= x;
    x += weyl += inc;
    return (int) (x = (x >>> 32) | (x << 32));
  }

  @Override
  public int nextInt(int n) {
    if (n <= 0) {
      throw new IllegalArgumentException("Not positive: " + n);
    }
    // Lemire (2019): Fast Random Integer Generation in an Interval
    // https://arxiv.org/abs/1805.10941
    long mult = (nextInt() & 0xffffffffL) * n;
    long left = mult & 0xffffffffL;
    if (left < n) {
      // 2^32 % n
      final long t = POW_32 % n;
      while (left < t) {
        mult = (nextInt() & 0xffffffffL) * n;
        left = mult & 0xffffffffL;
      }
    }
    return (int) (mult >>> 32);
  }

  @Override
  public long nextLong() {
    // Avoid round trip from long to int to long by performing two iterations inline
    x *= x;
    x += weyl += inc;
    final long i1 = x & 0xffffffff00000000L;
    x = (x >>> 32) | (x << 32);
    x *= x;
    x += weyl += inc;
    final long i2 = x >>> 32;
    x = i2 | x << 32;
    return i1 | i2;
  }

  @Override
  public long nextLong(long n) {
    if (n <= 0) {
      throw new IllegalArgumentException("Not positive: " + n);
    }
    final long nm1 = n - 1;
    if ((n & nm1) == 0) {
      // Power of 2
      return nextLong() & nm1;
    }
    long bits;
    long val;
    do {
      bits = nextLong() >>> 1;
      val = bits % n;
    } while (bits - val + nm1 < 0);

    return val;
  }

  @Override
  public boolean nextBoolean() {
    return nextInt() < 0;
  }

  @Override
  public float nextFloat() {
    return (nextInt() >>> 8) * 0x1.0p-24f;
  }

  @Override
  public double nextDouble() {
    return (nextLong() >>> 11) * 0x1.0p-53;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Creates a new state and increment for the new instance. The probability of increment
   * collision is 2<sup>-18</sup>; the probability of state collision is 2<sup>-64</sup>.
   *
   * <p>For the purposes of overlap computation this generator should be considered to have a period
   * of 2<sup>64</sup>.
   */
  @Override
  public MiddleSquareWeylSequence split() {
    final long increment = createIncrement(nextInt());
    // Seed the Weyl increment using the state of the generator
    final long weylState = Mixers.stafford13(x) ^ Mixers.stafford13(weyl);
    return new MiddleSquareWeylSequence(increment, weylState, increment);
  }

  @Override
  public RandomProviderState saveState() {
    return new MswsState(x, weyl, inc);
  }

  @Override
  public void restoreState(RandomProviderState state) {
    if (state instanceof MswsState) {
      final MswsState rngState = (MswsState) state;
      this.x = rngState.state;
      this.weyl = rngState.weyl;
      // Ensure the increment is odd
      this.inc = rngState.inc | 1L;
    } else {
      throw new IllegalArgumentException("Incompatible state");
    }
  }

  /**
   * The state of the generator.
   */
  private static class MswsState implements RandomProviderState {
    /** State of the generator. */
    final long state;
    /** State of the Weyl sequence. */
    final long weyl;
    /** Increment for the Weyl sequence. */
    final long inc;

    /**
     * Create a new instance.
     *
     * @param state the x
     * @param weylState the w
     * @param increment the s
     */
    MswsState(long state, long weylState, long increment) {
      this.state = state;
      this.weyl = weylState;
      this.inc = increment;
    }
  }
}
